package org.xbib.rdf;

import org.xbib.iri.namespace.IRINamespaceContext;

public interface RdfGraphParams extends RdfContentParams {

    IRINamespaceContext getNamespaceContext();

    boolean isWriteNamespaceContext();
}
