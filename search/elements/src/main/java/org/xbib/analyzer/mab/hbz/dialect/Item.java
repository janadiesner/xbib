package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class Item extends MABElement {
    
    private final static MABElement element = new Item();
    
    private Item() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
