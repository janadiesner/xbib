package org.xbib.json;

import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;

public class JsonNamespaceContext implements NamespaceContext {

    public int getNamespaceCount() {
        return 0;
    }

    public String getNamespaceURI(String prefix) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPrefix(String namespaceURI) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Iterator getPrefixes(String namespaceURI) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}