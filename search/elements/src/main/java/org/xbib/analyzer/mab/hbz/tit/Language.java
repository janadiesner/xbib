package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class Language extends MABElement {
    
    private final static MABElement element = new Language();
    
    private Language() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
