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
package org.xbib.entities.marc;

import org.xbib.entities.EntityQueue;
import org.xbib.entities.UnmappedKeyListener;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldList;
import org.xbib.rdf.RdfContentBuilderProvider;
import org.xbib.rdf.Resource;
import org.xbib.rdf.memory.MemoryRdfGraph;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MARCEntityQueue extends EntityQueue<MARCEntityBuilderState, MARCEntity, FieldList, String>
        implements Closeable {

    private final static Logger logger = LoggerFactory.getLogger(MARCEntityQueue.class.getName());

    private final static IRI tempPredicate = IRI.create("tmp");

    private List<RdfContentBuilderProvider> providers;

    private UnmappedKeyListener<FieldList> listener;

    public MARCEntityQueue(String path) {
        super(path, new MARCSpecification(), 1);
    }

    public MARCEntityQueue(String path, int workers) {
        super(path, new MARCSpecification(), workers);
    }

    public MARCEntityQueue(String path, Map<String, Object> params, int workers) {
        super(path, new MARCSpecification().setParameters(params), workers);
    }

    public MARCEntityQueue setUnmappedKeyListener(UnmappedKeyListener<FieldList> listener) {
        this.listener = listener;
        return this;
    }

    public MARCEntityQueue setContentBuilderProviders(RdfContentBuilderProvider... providers) {
        this.providers = Arrays.asList(providers);
        return this;
    }

    public MARCEntityQueue setContentBuilderProviders(List<RdfContentBuilderProvider> providers) {
        this.providers = providers;
        return this;
    }

    @Override
    public List<RdfContentBuilderProvider> contentBuilderProviders() {
        return providers;
    }

    @Override
    public void close() throws IOException {
        try {
            super.finish(60L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void beforeCompletion(MARCEntityBuilderState state) throws IOException {
        // empty
    }

    @Override
    public void afterCompletion(MARCEntityBuilderState state) throws IOException {
        //empty
    }

    @Override
    public MARCWorker newWorker() {
        return new MARCWorker();
    }

    public class MARCWorker extends EntityWorker {

        @Override
        public MARCEntityBuilderState newState() {
            return new MARCEntityBuilderState(new MemoryRdfGraph<>(), contentBuilderProviders());
        }

        @Override
        public MARCEntityBuilder newBuilder() {
            return new MARCEntityBuilder();
        }

        @Override
        public void build(FieldList fields, String value) throws IOException {
            if (fields == null) {
                return;
            }
            String key = fields.toKey();
            MARCEntity entity = null;
            try {
                entity = (MARCEntity) specification().getEntity(key, map());
                if (entity == null) {
                    entity = (MARCEntity) specification().getEntityByKey(key, map());
                }
            } catch (ClassCastException e) {
                logger.error("not a MARCEntity class instance for key " + key);
            }
            if (entity != null) {
                /*if (entity.map(fields)) {
                    key = fields.toKey();
                    try {
                        entity = (MARCEntity) specification().getEntity(key, map());
                        if (entity == null) {
                            entity = (MARCEntity) specification().getEntityByKey(key, map());
                        }
                    } catch (ClassCastException e) {
                        logger.error("not a MARCElement instance for key" + key);
                    }
                }*/
                // entity-based processing
                boolean done = entity.fields(this, fields, value);
                if (done) {
                    return;
                }
                // add entity to resource
                addToResource(state().getResource(), fields, entity, value);
                // complete other things for this entity, like facets etc.
                builder().build(state(), entity, fields, value);
            } else {
                if (listener != null) {
                    listener.unknown(fields);
                }
            }
            // complete no matter if an entity was found or not
            builder().build(state(), entity, fields, value);
        }

        public void addToResource(Resource resource, FieldList fields, MARCEntity entity, String value) throws IOException {
            // setup
            Map<String, Object> defaultSubfields = (Map<String, Object>) entity.getSettings().get("subfields");
            if (defaultSubfields == null) {
                return;
            }
            Map<Field, String> fieldNames = new HashMap<Field, String>();
            // create another anoymous resource, will be linked late if predicate is determined
            Resource newResource = state().getResource().newResource(tempPredicate);
            // default predicate is the name of the class
            String predicate = entity.getClass().getSimpleName();
            // the _predicate field allows to select a field to name the resource by a coded value
            if (entity.getSettings().containsKey("_predicate")) {
                predicate = (String) entity.getSettings().get("_predicate");
            }
            boolean overridePredicate = false;
            // put all found fields with configured subfield names to this resource
            for (Field field : fields) {
                // skip fields that have no data (invalid / degraded)
                if (field.data() == null || field.data().isEmpty()) {
                    continue;
                }
                Map<String, Object> subfields = defaultSubfields;
                // tag predicates defined?
                if (entity.getSettings().containsKey("tags")) {
                    Map<String, Object> tags = (Map<String, Object>) entity.getSettings().get("tags");
                    if (tags.containsKey(field.tag())) {
                        if (!overridePredicate) {
                            predicate = (String) tags.get(field.tag());
                        }
                        subfields = (Map<String, Object>) entity.getSettings().get(predicate);
                        if (subfields == null) {
                            subfields = defaultSubfields;
                        }
                    }
                }
                // indicator-based predicate defined?
                if (entity.getSettings().containsKey("indicators")) {
                    Map<String, Object> indicators = (Map<String, Object>) entity.getSettings().get("indicators");
                    if (indicators.containsKey(field.tag())) {
                        Map<String,Object> indicatorMap =  (Map<String,Object>)indicators.get(field.tag());
                        if (indicatorMap.containsKey(field.indicator())) {
                            if (!overridePredicate) {
                                predicate = (String) indicatorMap.get(field.indicator());
                                fieldNames.put(field, predicate);
                            }
                            subfields = (Map<String, Object>) entity.getSettings().get(predicate);
                            if (subfields == null) {
                                subfields = defaultSubfields;
                            }
                        }
                    }
                }
                // is there a subfield value decoder?
                Map.Entry<String, Object> me = SubfieldValueMapper.map(subfields, field);
                if (me.getKey() != null) {
                    String v = me.getValue().toString();
                    if (fieldNames.containsKey(field)) {
                        String fieldName = fieldNames.get(field);
                        // field-specific subfield map
                        Map<String, Object> vm = (Map<String, Object>) entity.getSettings().get(fieldName);
                        if (vm == null) {
                            // fallback to "subfields"
                            vm =  (Map<String, Object>) entity.getSettings().get("subfields");
                        }
                        // TODO not very exact code here. This tries to guess words.
                        int pos = v.indexOf(' '); // code must be non-blank word
                        String vv = pos > 0 ? v.substring(0, pos) : v;
                        // code table lookup
                        if (vm.containsKey(v)) {
                            newResource.add(me.getKey() + "Source", v);
                            v = (String) vm.get(v);
                        } else if (vm.containsKey(vv)) {
                            newResource.add(me.getKey() + "Source", v);
                            v = (String) vm.get(vv);
                        } else {
                            List<Map<String, String>> patterns = (List<Map<String, String>>) entity.getSettings().get(fieldName + "pattern");
                            if (patterns != null) {
                                for (Map<String, String> pattern : patterns) {
                                    Map.Entry<String, String> mme = pattern.entrySet().iterator().next();
                                    String p = mme.getKey();
                                    String rel = mme.getValue();
                                    Matcher m = Pattern.compile(p, Pattern.CASE_INSENSITIVE).matcher(v);
                                    if (m.matches()) {
                                        newResource.add(me.getKey() + "Source", v);
                                        v = rel;
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        // default subfield map
                        String fieldName = me.getKey();
                        if (entity.getSettings().containsKey(fieldName)) {
                            Map<String, Object> vm = (Map<String, Object>) entity.getSettings().get(fieldName);
                            int pos = v.indexOf(' ');
                            String vv = pos > 0 ? v.substring(0, pos) : v;
                            if (vm.containsKey(v)) {
                                newResource.add(me.getKey() + "Source", v);
                                v = (String) vm.get(v);
                            } else if (vm.containsKey(vv)) {
                                newResource.add(me.getKey() + "Source", v);
                                v = (String) vm.get(vv);
                            } else {
                                // relation by pattern?
                                List<Map<String, String>> patterns = (List<Map<String, String>>) entity.getSettings().get(fieldName + "pattern");
                                if (patterns != null) {
                                    for (Map<String, String> pattern : patterns) {
                                        Map.Entry<String, String> mme = pattern.entrySet().iterator().next();
                                        String p = mme.getKey();
                                        String rel = mme.getValue();
                                        Matcher m = Pattern.compile(p, Pattern.CASE_INSENSITIVE).matcher(v);
                                        if (m.matches()) {
                                            newResource.add(me.getKey() + "Source", v);
                                            v = rel;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // transform value v
                    v = entity.data(builder(), predicate, newResource, me.getKey(), v);
                    // is this the predicate field or a value?
                    if (me.getKey().equals(predicate)) {
                        predicate = v;
                        overridePredicate = true;
                    } else {
                        newResource.add(me.getKey(), v);
                    }
                } else {
                    // no decoder, simple add field data
                    String property = null;
                    String subfieldId = field.subfieldId();
                    if (subfieldId == null) {
                        subfieldId = ""; // empty string, for map lookup
                    }
                    try {
                        property = (String) subfields.get(subfieldId);
                    } catch (ClassCastException e) {
                        logger.error("cannot use string property of '" + subfieldId + "' for field " + field);
                    }
                    if (property == null) {
                        // unmapped subfield ID
                        property = subfieldId;
                    }
                    newResource.add(property, entity.data(builder(), predicate, newResource, property, field.data()));
                }
                // call custom add-ons
                entity.field(builder(), field, value);
            }
            // add child resource, now that we know the predicate
            //resource.add(predicate, newResource);
            resource.rename(tempPredicate, IRI.builder().curie(predicate).build());
        }
    }

}
