package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class Abstract extends MABElement {
    
    private final static MABElement element = new Abstract();
    
    private Abstract() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
