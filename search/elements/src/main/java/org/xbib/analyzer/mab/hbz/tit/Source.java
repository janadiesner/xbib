package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class Source extends MABElement {
    
    private final static MABElement element = new Source();
    
    private Source() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
