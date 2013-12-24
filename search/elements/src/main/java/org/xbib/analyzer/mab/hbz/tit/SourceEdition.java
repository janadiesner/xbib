package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SourceEdition extends MABElement {
    
    private final static MABElement element = new SourceEdition();
    
    private SourceEdition() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
