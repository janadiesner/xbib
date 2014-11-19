package org.xbib.rdf.io.rdfxml;

import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfContentParams;

public class RdfXmlContentParams implements RdfContentParams {

    private final IRINamespaceContext namespaceContext;

    private final boolean writeNamespaceContext;

    public final static RdfXmlContentParams DEFAULT_PARAMS = new RdfXmlContentParams(IRINamespaceContext.getInstance(), true);

    public RdfXmlContentParams(IRINamespaceContext namespaceContext, boolean writeNamespaceContext) {
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
