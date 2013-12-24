package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class ItemShelfmark extends MABElement {
    
    private final static MABElement element = new ItemShelfmark();
    
    private ItemShelfmark() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
