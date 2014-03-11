package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class Item extends MABElement {

    private final static MABElement element = new Item();

    private Item() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
