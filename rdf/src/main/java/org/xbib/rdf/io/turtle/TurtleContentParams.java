package org.xbib.rdf.io.turtle;

import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfContentParams;

public class TurtleContentParams implements RdfContentParams {

    private final IRINamespaceContext namespaceContext;

    private final boolean writeNamespaceContext;

    public final static TurtleContentParams DEFAULT_PARAMS = new TurtleContentParams(IRINamespaceContext.getInstance(), true);

    public TurtleContentParams(IRINamespaceContext namespaceContext, boolean writeNamespaceContext) {
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
