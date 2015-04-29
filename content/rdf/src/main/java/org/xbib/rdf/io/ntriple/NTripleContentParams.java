package org.xbib.rdf.io.ntriple;

import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfContentParams;

public class NTripleContentParams implements RdfContentParams {

    private final IRINamespaceContext namespaceContext;

    public final static NTripleContentParams DEFAULT_PARAMS = new NTripleContentParams(IRINamespaceContext.getInstance());

    public NTripleContentParams(IRINamespaceContext namespaceContext) {
        this.namespaceContext = namespaceContext;
    }

    public IRINamespaceContext getNamespaceContext() {
        return namespaceContext;
    }

}
