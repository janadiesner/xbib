package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SourceDescriptionVolume extends MABElement {
    
    private final static MABElement element = new SourceDescriptionVolume();
    
    private SourceDescriptionVolume() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
