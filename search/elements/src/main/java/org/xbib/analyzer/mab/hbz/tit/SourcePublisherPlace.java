package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SourcePublisherPlace extends MABElement {
    
    private final static MABElement element = new SourcePublisherPlace();
    
    private SourcePublisherPlace() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
