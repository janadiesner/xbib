package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class CartographicExtent extends MABElement {

    private final static MABElement element = new CartographicExtent();

    private CartographicExtent() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
