package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class ItemCallnumber extends MABElement {
    
    private final static MABElement element = new ItemCallnumber();
    
    private ItemCallnumber() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
