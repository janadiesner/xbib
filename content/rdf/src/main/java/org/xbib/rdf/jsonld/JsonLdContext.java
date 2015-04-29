/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.rdf.jsonld;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.iri.IRI;
import org.xbib.iri.MalformedIRIException;
import org.xbib.rdf.io.sink.QuadSink;
import org.xbib.rdf.JsonLd;
import org.xbib.rdf.RDF;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Provides context for IRI resolving, holds processing state for each JSON-LD node.
 */
final class JsonLdContext implements RDF, JsonLd {

    private final static Logger logger = LogManager.getLogger(JsonLdContext.class);

    static final int ID_DECLARED = 1;

    static final int CONTEXT_DECLARED = 2;

    static final int PARENT_SAFE = 4;

    static final int SAFE_TO_SINK_TRIPLES = ID_DECLARED | CONTEXT_DECLARED | PARENT_SAFE;

    static class BNodeContext {
        String iri;
        private int nextBnodeId;

        BNodeContext() {
            nextBnodeId = 0;
        }

        String createBnode(boolean shortenable) {
            return shortenable ? BNODE_PREFIX + 'b' + (nextBnodeId++) + SHORTENABLE_BNODE_SUFFIX : BNODE_PREFIX + 'b' + nextBnodeId++;
        }

        void clear() {
            iri = null;
        }
    }

    String base;
    String id;
    String graph;
    String subject;
    String predicate;
    String vocab;
    String lang;
    String objectLit;
    String objectLitDt;
    String listTail;
    boolean parsingArray;
    String containerType;
    boolean nullified;
    boolean hasProps;
    boolean reversed;

    boolean wrapped;
    boolean index;

    boolean hasNonGraphContextProps;

    JsonLdContext parent;

    private int state;

    private final QuadSink sink;

    private final BNodeContext bNodeContext;

    private final Map<String, String> iriMappings = new TreeMap<String, String>();

    private final Map<String, String> dtMappings = new TreeMap<String, String>();

    private final Map<String, String> langMappings = new TreeMap<String, String>();

    private final Collection<JsonLdContext> children = new ArrayList<JsonLdContext>();

    private final Queue<String> nonLiteralQueue = new LinkedList<String>();

    private final Queue<String> plainLiteralQueue = new LinkedList<String>();

    private final Queue<String> typedLiteralQueue = new LinkedList<String>();

    private JsonLdContext(QuadSink sink, JsonLdContext parent, BNodeContext bNodeContext) {
        this.sink = sink;
        this.parent = parent;
        this.bNodeContext = bNodeContext;
    }

    static JsonLdContext createInitialContext(QuadSink sink, BNodeContext bNodeContext) {
        JsonLdContext initialContext = new JsonLdContext(sink, null, bNodeContext);
        initialContext.base = DOC_IRI;
        initialContext.subject = ".";
        initialContext.state = SAFE_TO_SINK_TRIPLES;
        return initialContext;
    }

    static BNodeContext createBNodeContext() {
        return new BNodeContext();
    }

    JsonLdContext initChildContext(String graph) {
        JsonLdContext child = new JsonLdContext(sink, this, bNodeContext);
        child.lang = this.lang;
        child.subject = bNodeContext.createBnode(false);
        child.graph = this.graph;
        child.vocab = this.vocab;
        children.add(child);
        if (graph != null) {
            child.graph = graph;
        }
        return child;
    }

    void nullify() {
        iriMappings.clear();
        dtMappings.clear();
        langMappings.clear();
        lang = null;
        nullified = true;
        base = bNodeContext.iri;
    }

    boolean isPredicateKeyword() {
        return predicate.charAt(0) == '@';
    }

    void defineIriMappingForPredicate(String value) {
        if (vocab != null && value != null && value.indexOf(':') == -1) {
            value = vocab + value;
        }
        iriMappings.put(predicate, value);
        if (!dtMappings.containsKey(predicate)) {
            dtMappings.put(predicate, null);
        }
        if (!langMappings.containsKey(predicate)) {
            langMappings.put(predicate, null);
        }
    }

    void id(String value) {
        this.id = value;
    }

    void defineDtMappingForPredicate(String value) {
        dtMappings.put(predicate, value);
    }

    String getDtMapping(String value) {
        if (dtMappings.containsKey(value)) {
            return dtMappings.get(value);
        }
        if (!nullified && parent != null) {
            return parent.getDtMapping(value);
        }
        return null;
    }

    public void defineLangMappingForPredicate(String value) {
        langMappings.put(predicate, value);
    }

    private String getLangMapping(String value) {
        if (langMappings.containsKey(value)) {
            return langMappings.get(value);
        }
        if (!nullified && parent != null) {
            return parent.getLangMapping(value);
        }
        return null;
    }

    private String getBase() {
        if (base != null) {
            return base;
        }
        if (!nullified && parent != null) {
            return parent.getBase();
        }
        return null;
    }

    void updateState(int state) throws IOException {
        this.state |= state;
        if (this.state == SAFE_TO_SINK_TRIPLES) {
            for (JsonLdContext child : children.toArray(new JsonLdContext[children.size()])) {
                child.updateState(PARENT_SAFE);
            }
            if (children.isEmpty()) {
                sinkUnsafeTriples();
            }
        }
    }

    private void sinkUnsafeTriples() throws IOException {
        try {
            if (!subject.startsWith(BNODE_PREFIX)) {
                subject = resolveCurieOrIri(subject, false);
            }
            graph = resolve(graph, false, false);
        } catch (MalformedIRIException e) {
            logger.warn(e.getMessage(), e);
            nonLiteralQueue.clear();
            plainLiteralQueue.clear();
            typedLiteralQueue.clear();
        }
        if (id !=null) {
            sink.beginDocument(id);
        }
        while (!nonLiteralQueue.isEmpty()) {
            addNonLiteralUnsafe(nonLiteralQueue.poll(), nonLiteralQueue.poll(), nonLiteralQueue.poll());
        }
        while (!plainLiteralQueue.isEmpty()) {
            addPlainLiteralUnsafe(plainLiteralQueue.poll(), plainLiteralQueue.poll(), plainLiteralQueue.poll());
        }
        while (!typedLiteralQueue.isEmpty()) {
            addTypedLiteralUnsafe(typedLiteralQueue.poll(), typedLiteralQueue.poll(), typedLiteralQueue.poll());
        }
        if (id != null) {
            sink.endDocument(id);
        }
        if (parent != null) {
            parent.children.remove(this);
        }
    }

    public void addListFirst(String object) throws IOException {
        if (listTail.equals(subject)) {
            if (state == SAFE_TO_SINK_TRIPLES) {
                addPlainLiteralUnsafe(FIRST, object, lang);
            } else {
                plainLiteralQueue.offer(FIRST);
                plainLiteralQueue.offer(object);
                plainLiteralQueue.offer(lang);
            }
        } else {
            sink.addPlainLiteral(listTail, FIRST, object, lang, graph);
        }
    }

    public void addListFirst(String object, String dt) throws IOException {
        if (listTail.equals(subject)) {
            if (state == SAFE_TO_SINK_TRIPLES) {
                addTypedLiteralUnsafe(FIRST, object, dt);
            } else {
                typedLiteralQueue.offer(FIRST);
                typedLiteralQueue.offer(object);
                typedLiteralQueue.offer(dt);
            }
        } else {
            sink.addTypedLiteral(listTail, FIRST, object, dt, graph);
        }
    }

    public void addListRest(String object) throws IOException {
        if (listTail.equals(subject)) {
            if (state == SAFE_TO_SINK_TRIPLES) {
                addNonLiteralUnsafe(REST, object, null);
            } else {
                nonLiteralQueue.offer(REST);
                nonLiteralQueue.offer(object);
                nonLiteralQueue.offer(null);
            }
        } else {
            sink.addNonLiteral(listTail, REST, object, graph);
        }
        listTail = object;
    }

    public void addToSet(String object) throws IOException {
        parent.addPlainLiteral(object, lang);
    }

    public void addToSet(String object, String dt) throws IOException {
        parent.addTypedLiteral(object, dt);
    }

    void addNonLiteral(String predicate, String object, String base) throws IOException {
        if (parent != null && predicate.equals(SET_KEY)) {
            parent.addNonLiteral(predicate, object, base);
        } else if (state == SAFE_TO_SINK_TRIPLES) {
            addNonLiteralUnsafe(predicate, object, base);
        } else {
            nonLiteralQueue.offer(predicate);
            nonLiteralQueue.offer(object);
            nonLiteralQueue.offer(base);
        }
    }

    void addNonLiteralUnsafe(String predicate, String object, String base) throws IOException {
        if (object == null) {
            return;
        }
        if (predicate.indexOf('@') >= 0) {
            return;
        }
        try {
            boolean reversed = this.reversed ^ REVERSE_KEY.equals(getDtMapping(predicate));
            String resolvedPredicate = resolve(predicate);
            String oldBase = this.base;
            if (base != null) {
                this.base = base;
            }
            object = resolve(object, false, resolvedPredicate.equals(TYPE));
            this.base = oldBase;
            if (reversed) {
                sink.addNonLiteral(object, resolvedPredicate, subject, graph);
            } else {
                sink.addNonLiteral(subject, resolvedPredicate, object, graph);
            }
        } catch (MalformedIRIException e) {
            //logger.warn("malformed IRI: [{},{},{}] {}", predicate, object, base, e.getMessage());
        }
    }

    void addPlainLiteral(String object, String lang) throws IOException {
        if (parent != null && predicate.equals(SET_KEY)) {
            parent.addPlainLiteral(object, lang);
        } else if (state == SAFE_TO_SINK_TRIPLES) {
            addPlainLiteralUnsafe(predicate, object, lang);
        } else {
            plainLiteralQueue.offer(predicate);
            plainLiteralQueue.offer(object);
            plainLiteralQueue.offer(lang);
        }
    }

    void addPlainLiteralUnsafe(String predicate, String object, String lang) throws IOException {
        try {
            String dt = getDtMapping(predicate);
            if (dt != null) {
                if (ID_KEY.equals(dt)) {
                    addNonLiteralUnsafe(predicate, object, null);
                    return;
                } else if (!dt.startsWith("@")) {
                    addTypedLiteralUnsafe(predicate, object, dt);
                    return;
                }
            }
            String resolvedLang = lang;
            if (LANGUAGE_KEY.equals(lang)) {
                resolvedLang = getLangMapping(predicate);
                if (NULL.equals(resolvedLang)) {
                    resolvedLang = null;
                } else if (resolvedLang == null) {
                    resolvedLang = this.lang;
                }
            }
            boolean reversed = this.reversed ^ REVERSE_KEY.equals(getDtMapping(predicate));
            if (reversed) {
                sink.addNonLiteral(object, resolve(predicate), subject, graph);
            } else {
                sink.addPlainLiteral(subject, resolve(predicate), object, resolvedLang, graph);
            }
        } catch (MalformedIRIException e) {
            //logger.warn("malformed IRI {}", e.getMessage());
        }
    }

    void addTypedLiteral(String object, String dt) throws IOException {
        if (parent != null && predicate.equals(SET_KEY)) {
            parent.addTypedLiteral(object, dt);
        } else if (state == SAFE_TO_SINK_TRIPLES) {
            addTypedLiteralUnsafe(predicate, object, dt);
        } else {
            typedLiteralQueue.offer(predicate);
            typedLiteralQueue.offer(object);
            typedLiteralQueue.offer(dt);
        }
    }

    private void addTypedLiteralUnsafe(String predicate, String object, String dt) throws IOException {
        try {
            boolean reversed = this.reversed ^ REVERSE_KEY.equals(getDtMapping(predicate));
            if (reversed) {
                sink.addNonLiteral(object, resolve(predicate), subject, graph);
            } else {
                sink.addTypedLiteral(subject, resolve(predicate), object, resolve(dt), graph);
            }
        } catch (MalformedIRIException e) {
            //logger.warn("malformed IRI {}", e.getMessage());
        }
    }

    private String resolve(String value) throws IOException {
        return resolve(value, true, true);
    }

    private String resolve(String value, boolean ignoreRelIri, boolean useVocab) throws IOException {
        if (value == null || value.startsWith(BNODE_PREFIX)) {
            return value;
        }
        if (value.isEmpty()) {
            throw new MalformedIRIException("empty IRI");
        }
        try {
            String mapping = resolveMapping(value, useVocab);
            if (mapping != null && mapping.charAt(0) != '@') {
                if (mapping.startsWith(BNODE_PREFIX)) {
                    return mapping;
                }
                return resolveCurieOrIri(mapping, false);
            }
        } catch (MalformedIRIException e) {
            //logger.warn("malformed IRI when resolve: {}", e.getMessage());
        }
        return resolveCurieOrIri(value, ignoreRelIri);
    }

    String resolveMapping(String value) throws IOException {
        return resolveMapping(value, true);
    }

    String resolveMapping(String value, boolean useVocab) throws IOException {
        if (iriMappings.containsKey(value)) {
            return iriMappings.get(value);
        }
        if (!nullified && parent != null) {
            try {
                return parent.resolveMapping(value, false);
            } catch (MalformedIRIException e) {
                //logger.warn("malformed IRI when resolving mapping: {}", e.getMessage());
            }
        }
        if (useVocab && vocab != null && value.indexOf(':') == -1) {
            return vocab + value;
        }
        throw new MalformedIRIException("can't resolve term " + value);
    }

    String resolveCurieOrIri(String curie, boolean ignoreRelIri) throws IOException {
        if (!ignoreRelIri && (curie == null || curie.isEmpty())) {
            return resolveIri(curie);
        }
        int delimPos = curie.indexOf(':');
        if (delimPos == -1) {
            if (ignoreRelIri) {
                throw new MalformedIRIException("CURIE with no prefix (" + curie + ") found");
            }
            return resolveIri(curie);
        }
        String suffix = curie.substring(delimPos + 1);
        if (suffix.startsWith("//")) {
            return resolveIri(curie);
        }
        String prefix = curie.substring(0, delimPos);
        if (prefix.equals("_")) {
            throw new MalformedIRIException("CURIE with invalid prefix (" + curie + ") found");
        }
        try {
            String prefixUri = resolveMapping(prefix);
            if (prefixUri != null) {
                return prefixUri + suffix;
            } else if (isIri(curie)) {
                return curie;
            }
        } catch (MalformedIRIException e) {
            if (isIri(curie)) {
                return curie;
            }
        }
        throw new MalformedIRIException("malformed IRI " + curie);
    }

    private String resolveIri(String iri) throws IOException {
        String base = getBase();
        if (DOC_IRI.equals(base)) {
            return resolveIri(bNodeContext.iri, iri);
        } else {
            return resolveIri(base, iri);
        }
    }

    public boolean isParsingContext() {
        return parent != null && (CONTEXT_KEY.equals(parent.predicate)
                || parent.parent != null && CONTEXT_KEY.equals(parent.parent.predicate));
    }

    public void processContext(JsonLdContext context) throws IOException {
        iriMappings.putAll(context.iriMappings);
        dtMappings.putAll(context.dtMappings);
        langMappings.putAll(context.langMappings);
        lang = context.lang;
        base = context.base;
        vocab = context.vocab;
        updateState(CONTEXT_DECLARED);
        children.remove(context);
    }

    private static final Pattern ABS_OPAQUE_IRI_PATTERN = Pattern.compile(
            // scheme
            "[a-zA-Z][a-zA-Z0-9+.-]*:"
                    // opaque part
                    + "[^#/][^#]*",
            Pattern.DOTALL);

    private static final Pattern ABS_HIER_IRI_PATTERN = Pattern.compile(
            // scheme
            "[a-zA-Z][a-zA-Z0-9+.-]*:"
                    // user
                    + "/{1,3}(([^/?#@]*)@)?"
                    // host
                    + "(\\[[^@/?#]+\\]|([^@/?#:]+))"
                    // port
                    + "(:([^/?#]*))?"
                    // path
                    + "([^#?]*)?"
                    // query
                    + "(\\?([^#]*))?"
                    // fragment
                    + "(#[^#]*)?",
            Pattern.DOTALL);

    private static final Pattern URN_PATTERN = Pattern.compile("urn:[a-zA-Z0-9][a-zA-Z0-9-]{1,31}:.+");

    /**
     * Resolves specified IRI. Absolute IRI are returned unmodified
     *
     * @param base base to resolve against
     * @param iri  IRI to be resolved
     * @return resolved absolute IRI
     * @throws MalformedIRIException
     */
    private static String resolveIri(String base, String iri) throws MalformedIRIException {
        if (iri == null) {
            return null;
        }
        if (isIri(iri) || isUrn(iri)) {
            return iri;
        } else {
            if (iri.startsWith("?") || iri.isEmpty()) {
                if (base == null) {
                    return iri;
                }
                if (base.endsWith("#")) {
                    return base.substring(0, base.length() - 1) + iri;
                }
                return base + iri;
            }
            String result;
            try {
                URL basePart = new URL(base);
                result = new URL(basePart, iri).toString();
            } catch (MalformedURLException e) {
                result = base + iri;
            }
            if (isIri(result)) {
                return result;
            }
            throw new MalformedIRIException("malformed IRI " + iri);
        }
    }

    /**
     * Checks if specified string is IRI
     *
     * @param value value to check
     * @return true if value is IRI
     */
    private static boolean isIri(String value) {
        return ABS_HIER_IRI_PATTERN.matcher(value).matches() || ABS_OPAQUE_IRI_PATTERN.matcher(value).matches();
    }

    /**
     * Checks if specified string is URN
     *
     * @param value value to check
     * @return true if value is URN
     */
    private static boolean isUrn(String value) {
        return URN_PATTERN.matcher(value).matches();
    }

}
