package org.xbib.rdf.io.xml;

import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfContentParams;

public class XmlContentParams implements RdfContentParams {

    private final IRINamespaceContext namespaceContext;

    private final boolean writeNamespaceContext;

    public final static XmlContentParams DEFAULT_PARAMS = new XmlContentParams(IRINamespaceContext.getInstance(), true);

    public XmlContentParams(IRINamespaceContext namespaceContext, boolean writeNamespaceContext) {
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
