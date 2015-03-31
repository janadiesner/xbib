package org.semarglproject.jsonld;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semarglproject.sink.QuadSink;
import org.semarglproject.vocab.JSONLD;
import org.semarglproject.vocab.RDF;
import org.semarglproject.vocab.XSD;

import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * Handler class for JsonLdParser. Handles events in SAX-like manner.
 */
final class JsonLdContentHandler implements JSONLD, XSD {

    private final static Logger logger = LogManager.getLogger(JsonLdContentHandler.class);

    private static final Pattern TERM_PATTERN = Pattern.compile("[a-zA-Z0-9_-]+", Pattern.DOTALL);

    private final static String CONTAINER_LIST_KEY = "@container@list";

    private static final String CONTAINER_INDEX_KEY = "@container@index";

    private final static String CONTAINER_LANGUAGE_KEY = "@container@language";

    private final static String CONTAINER_SET_KEY = "@container@set";

    private Deque<Context> contextStack = new LinkedList<Context>();

    private Context currentContext;

    private final QuadSink sink;

    public JsonLdContentHandler(QuadSink sink) {
        this.sink = sink;
    }

    public void onDocumentStart() {
        currentContext = Context.createInitialContext(sink);
    }

    public void onDocumentEnd() {
        clear();
    }

    public void onObjectStart() throws IOException {
        String graph = null;
        if (GRAPH_KEY.equals(currentContext.predicate)
                && (contextStack.size() > 1 || currentContext.hasNonGraphContextProps)) {
            graph = currentContext.subject;
        }
        contextStack.push(currentContext);
        currentContext = currentContext.initChildContext(graph);
        if (contextStack.size() == 1) {
            currentContext.updateState(Context.PARENT_SAFE);
        }
        if (REVERSE_KEY.equals(currentContext.parent.predicate)) {
            currentContext.subject = currentContext.parent.subject;
            currentContext.reversed = true;
            currentContext.containerType = REVERSE_KEY;
            currentContext.updateState(Context.ID_DECLARED);
        } else if (contextStack.size() > 1) {
            String dt = currentContext.getDtMapping(currentContext.parent.predicate);
            if (CONTAINER_INDEX_KEY.equals(dt)) {
                currentContext.subject = currentContext.parent.subject;
                currentContext.index = true;
            }
        }
    }

    public void onObjectEnd() throws IOException {
        unwrap();
        if (currentContext.objectLit != null) {
            // ignore floating values
            if (contextStack.size() > 1 && !NULL.equals(currentContext.objectLit)) {
                if (currentContext.objectLitDt != null) {
                    currentContext.parent.addTypedLiteral(currentContext.objectLit, currentContext.objectLitDt);
                } else {
                    currentContext.parent.addPlainLiteral(currentContext.objectLit, currentContext.lang);
                }
            }
            // currentContext remove can be forced because literal nodes don't contain any unsafe triples to sink
            currentContext.updateState(Context.PARENT_SAFE);
        } else if (!currentContext.isParsingContext() && !currentContext.index) {
            addSubjectTypeDefinition(currentContext.objectLitDt, currentContext.base);
            if (contextStack.size() > 1 && currentContext.containerType == null) {
                // TODO: check for property reordering issues
                addSubjectTypeDefinition(currentContext.parent.getDtMapping(currentContext.parent.predicate),
                        currentContext.parent.base);
                if (!SET_KEY.equals(currentContext.parent.predicate) || currentContext.hasProps) {
                    currentContext.parent.addNonLiteral(currentContext.parent.predicate,
                            currentContext.subject, currentContext.base);
                }
            }
        }
        boolean nullObject = !currentContext.hasProps && NULL.equals(currentContext.subject);
        if (currentContext.isParsingContext()) {
            currentContext.parent.processContext(currentContext);
        }
        currentContext.updateState(Context.ID_DECLARED | Context.CONTEXT_DECLARED);
        currentContext = contextStack.pop();
        if (nullObject) {
            onNull();
        }
    }

    public void onArrayStart() {
        currentContext.parsingArray = true;
    }

    public void onArrayEnd() throws IOException {
        currentContext.parsingArray = false;
        if (LIST_KEY.equals(currentContext.predicate)) {
            if (currentContext.listTail != null) {
                currentContext.addListRest(RDF.NIL);
            } else {
                currentContext.subject = RDF.NIL;
                currentContext.containerType = null;
            }
        } else if (SET_KEY.equals(currentContext.predicate)) {
            currentContext.objectLit = NULL;
        } else if (currentContext.predicate != null) {
            String dt = currentContext.getDtMapping(currentContext.predicate);
            if (CONTAINER_LIST_KEY.equals(dt)) {
                try {
                    currentContext.addNonLiteral(currentContext.resolveMapping(currentContext.predicate), RDF.NIL,
                            currentContext.base);
                } catch (MalformedIRIException e) {
                    //
                }
            }
        }
    }

    private void unwrap() throws IOException {
        if (currentContext.parsingArray) {
            onArrayEnd();
        }
        if (!currentContext.wrapped) {
            return;
        }
        currentContext.wrapped = false;
        onObjectEnd();
    }

    public void onKey(String key) throws IOException {
        unwrap();
        if (currentContext.index && !key.startsWith("@")) {
            key = currentContext.parent.predicate;
        } else if (currentContext.parent != null && currentContext.parent.predicate != null) {
            String dt = currentContext.getDtMapping(currentContext.parent.predicate);
            if (CONTAINER_LANGUAGE_KEY.equals(dt)) {
                currentContext.lang = key;
                key = currentContext.parent.predicate;
                currentContext.containerType = LANGUAGE_KEY;
                currentContext.subject = currentContext.parent.subject;
            }
        }
        try {
            String mapping = currentContext.resolveMapping(key);
            try {
                if (mapping != null) {
                    // we need to go deeper... in case of keyword aliases in term definitions
                    mapping = currentContext.resolveMapping(mapping);
                }
            } catch (MalformedIRIException e) {
                //
            }
            if (mapping != null && mapping.charAt(0) == '@') {
                currentContext.predicate = mapping;
                if (mapping.equals(SET_KEY) || mapping.equals(LIST_KEY)) {
                    currentContext.containerType = mapping;
                }
            } else {
                currentContext.predicate = key;
            }
        } catch (MalformedIRIException e) {
            currentContext.predicate = key;
        }
        if (SET_KEY.equals(currentContext.predicate) || LIST_KEY.equals(currentContext.predicate)) {
            onArrayStart();
        }
        if (!GRAPH_KEY.equals(currentContext.predicate) && !CONTEXT_KEY.equals(currentContext.predicate)) {
            currentContext.hasNonGraphContextProps = true;
            if (!currentContext.predicate.startsWith("@")) {
                currentContext.hasProps = true;
            }
        }
    }

    public void onString(String value) throws IOException {
        if (currentContext.isParsingContext()) {
            Context parentContext = currentContext.parent;
            if (parentContext.isParsingContext()) {
                if (ID_KEY.equals(currentContext.predicate)) {
                    parentContext.defineIriMappingForPredicate(value);
                } else if (TYPE_KEY.equals(currentContext.predicate)) {
                    parentContext.defineDtMappingForPredicate(value);
                } else if (LANGUAGE_KEY.equals(currentContext.predicate)) {
                    parentContext.defineLangMappingForPredicate(value);
                } else if (CONTAINER_KEY.equals(currentContext.predicate)) {
                    parentContext.defineDtMappingForPredicate(CONTAINER_KEY + value);
                } else if (REVERSE_KEY.equals(currentContext.predicate)) {
                    parentContext.defineIriMappingForPredicate(value);
                    parentContext.defineDtMappingForPredicate(REVERSE_KEY);
                }
                return;
            } else if (!currentContext.isPredicateKeyword()) {
                currentContext.defineIriMappingForPredicate(value);
                return;
            } else if (BASE_KEY.equals(currentContext.predicate)) {
                currentContext.base = value;
                return;
            } else if (VOCAB_KEY.equals(currentContext.predicate)) {
                currentContext.vocab = value;
                return;
            }
        } else if (!currentContext.isPredicateKeyword() && currentContext.predicate != null) {
            // TODO: check for property reordering issues
            String dt = currentContext.getDtMapping(currentContext.predicate);
            if (CONTAINER_LIST_KEY.equals(dt)) {
                onObjectStart();
                onKey(LIST_KEY);
                onArrayStart();
                onString(value);
                currentContext.wrapped = true;
            } else if (VOCAB_KEY.equals(dt)) {
                String valueMapping;
                try {
                    valueMapping = currentContext.resolveMapping(value);
                } catch (MalformedIRIException e) {
                    valueMapping = value;
                }
                currentContext.addNonLiteral(currentContext.predicate, valueMapping, currentContext.base);
            } else if (ID_KEY.equals(dt)) {
                try {
                    String resolvedValue = currentContext.resolveCurieOrIri(value, false);
                    currentContext.addNonLiteral(currentContext.predicate, resolvedValue, currentContext.base);
                } catch (MalformedIRIException e) {
                    currentContext.addPlainLiteral(value, LANGUAGE_KEY);
                }
            } else if (LANGUAGE_KEY.equals(currentContext.containerType)) {
                currentContext.addPlainLiteral(value, currentContext.lang);
            } else {
                currentContext.addPlainLiteral(value, LANGUAGE_KEY);
            }
            return;
        }
        if (currentContext.isPredicateKeyword()) {
            if (TYPE_KEY.equals(currentContext.predicate)) {
                if (currentContext.parsingArray) {
                    addSubjectTypeDefinition(value, currentContext.base);
                } else {
                    currentContext.objectLitDt = value;
                }
            } else if (LANGUAGE_KEY.equals(currentContext.predicate)) {
                currentContext.lang = value;
            } else if (ID_KEY.equals(currentContext.predicate)) {
                if (currentContext.index) {
                    currentContext.addNonLiteral(currentContext.parent.predicate, value, currentContext.base);
                } else if (TERM_PATTERN.matcher(value).matches()) {
                    // force terms to be not considered in @id
                    currentContext.subject = "./" + value;
                } else {
                    currentContext.subject = value;
                }
                currentContext.updateState(Context.ID_DECLARED);
            } else if (VALUE_KEY.equals(currentContext.predicate)) {
                currentContext.objectLit = value;
            } else if (LIST_KEY.equals(currentContext.predicate) && isNotFloating()) {
                if (currentContext.listTail == null) {
                    currentContext.listTail = currentContext.subject;
                    currentContext.addListFirst(value);
                } else {
                    currentContext.addListRest(currentContext.createBnode(false));
                    currentContext.addListFirst(value);
                }
            } else if (SET_KEY.equals(currentContext.predicate) && isNotFloating()) {
                currentContext.addToSet(value);
            }
        }
    }

    private boolean isNotFloating() {
        return currentContext.parent != null && currentContext.parent.predicate != null &&
                !currentContext.parent.predicate.startsWith("@");
    }

    private void addSubjectTypeDefinition(String dt, String base) throws IOException {
        if (dt == null || dt.charAt(0) == '@') {
            return;
        }
        currentContext.addNonLiteral(RDF.TYPE, dt, base);
    }

    public void onBoolean(boolean value) throws IOException {
        processTypedValue(Boolean.toString(value), BOOLEAN);
    }

    public void onNull() {
        if (CONTEXT_KEY.equals(currentContext.predicate)) {
            currentContext.nullify();
        } else if (VALUE_KEY.equals(currentContext.predicate)) {
            currentContext.objectLit = NULL;
        } else if (ID_KEY.equals(currentContext.predicate)) {
            currentContext.subject = NULL;
        } else if (currentContext.isParsingContext()) {
            Context parentContext = currentContext.parent;
            if (parentContext.isParsingContext()) {
                if (LANGUAGE_KEY.equals(currentContext.predicate)) {
                    parentContext.defineLangMappingForPredicate(NULL);
                }
            } else {
                if (LANGUAGE_KEY.equals(currentContext.predicate)) {
                    currentContext.lang = null;
                } else if (BASE_KEY.equals(currentContext.predicate)) {
                    currentContext.base = DOC_IRI;
                } else if (VOCAB_KEY.equals(currentContext.predicate)) {
                    currentContext.vocab = null;
                } else {
                    currentContext.defineIriMappingForPredicate(null);
                }
            }
        }
    }

    public void onNumber(double value) throws IOException {
        processTypedValue(Double.toString(value), DOUBLE);
    }

    public void onNumber(int value) throws IOException {
        processTypedValue(Integer.toString(value), INTEGER);
    }

    public void processTypedValue(String value, String defaultDt) throws IOException {
        String predicateDt = currentContext.getDtMapping(currentContext.predicate);
        if (CONTAINER_LIST_KEY.equals(predicateDt)) {
            onObjectStart();
            onKey(LIST_KEY);
            onArrayStart();
            currentContext.wrapped = true;
        } else if (CONTAINER_SET_KEY.equals(predicateDt)) {
            onObjectStart();
            onKey(SET_KEY);
            onArrayStart();
            currentContext.wrapped = true;
        }
        String dt = currentContext.getDtMapping(currentContext.predicate);
        if (dt == null) {
            dt = defaultDt;
        }
        if (LIST_KEY.equals(currentContext.predicate) && isNotFloating()) {
            if (currentContext.listTail == null) {
                currentContext.listTail = currentContext.subject;
                currentContext.addListFirst(value, dt);
            } else {
                currentContext.addListRest(currentContext.createBnode(false));
                currentContext.addListFirst(value, dt);
            }
        } else if (SET_KEY.equals(currentContext.predicate) && isNotFloating()) {
            currentContext.addToSet(value, dt);
        } else {
            currentContext.addTypedLiteral(value, dt);
        }
    }

    private void clear() {
        currentContext.clear();
        contextStack.clear();
        currentContext = null;
    }

    public void setBaseUri(String baseUri) {
        currentContext.iri = baseUri;
    }
}
