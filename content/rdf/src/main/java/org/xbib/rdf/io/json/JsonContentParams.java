package org.xbib.rdf.io.json;

import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfContentParams;
import org.xbib.rdf.io.xml.XmlContentParams;

public class JsonContentParams extends XmlContentParams implements RdfContentParams {

    public final static JsonContentParams DEFAULT_PARAMS = new JsonContentParams(IRINamespaceContext.getInstance(), true);

    public JsonContentParams(IRINamespaceContext namespaceContext, boolean writeNamespaceContext) {
        super(namespaceContext, writeNamespaceContext);
    }
}
