package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SourceTitleWhole extends MABElement {
    
    private final static MABElement element = new SourceTitleWhole();
    
    private SourceTitleWhole() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}