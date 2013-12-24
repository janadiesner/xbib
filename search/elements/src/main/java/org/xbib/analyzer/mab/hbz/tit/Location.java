package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class Location extends MABElement {
    
    private final static MABElement element = new Location();
    
    private Location() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
