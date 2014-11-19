package org.xbib.rdf.io.ntriple;

import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfContentParams;

public class NTripleContentParams implements RdfContentParams {

    private final IRINamespaceContext namespaceContext;

    private final boolean writeNamespaceContext;

    public final static NTripleContentParams DEFAULT_PARAMS = new NTripleContentParams(IRINamespaceContext.getInstance(), true);

    public NTripleContentParams(IRINamespaceContext namespaceContext, boolean writeNamespaceContext) {
        this.namespaceContext = namespaceContext;
        this.writeNamespaceContext = writeNamespaceContext;
    }

    public IRINamespaceContext getNamespaceContext() {
        return namespaceContext;
    }

    public boolean isWriteNamespaceContext() {
        return writeNamespaceContext;
    }
}
