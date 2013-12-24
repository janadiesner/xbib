package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class ItemStatus extends MABElement {
    
    private final static MABElement element = new ItemStatus();
    
    private ItemStatus() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
