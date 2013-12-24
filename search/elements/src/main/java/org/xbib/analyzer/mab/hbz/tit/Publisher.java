package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class Publisher extends MABElement {
    
    private final static MABElement element = new Publisher();
    
    private Publisher() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
