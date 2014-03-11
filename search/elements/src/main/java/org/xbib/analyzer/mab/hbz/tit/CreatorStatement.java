package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class CreatorStatement extends MABElement {
    
    private final static MABElement element = new CreatorStatement();
    
    private CreatorStatement() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
