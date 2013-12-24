package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class DescriptionThesis extends MABElement {
    
    private final static MABElement element = new DescriptionThesis();
    
    private DescriptionThesis() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
