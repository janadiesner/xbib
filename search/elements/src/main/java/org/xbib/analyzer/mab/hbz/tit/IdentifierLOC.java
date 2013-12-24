package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class IdentifierLOC extends MABElement {
    
    private final static MABElement element = new IdentifierLOC();
    
    private IdentifierLOC() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
