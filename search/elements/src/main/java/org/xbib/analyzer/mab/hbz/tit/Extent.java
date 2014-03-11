package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class Extent extends MABElement {
    
    private final static MABElement element = new Extent();
    
    private Extent() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
