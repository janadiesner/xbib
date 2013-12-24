package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class PublisherPlace extends MABElement {
    
    private final static MABElement element = new PublisherPlace();
    
    private PublisherPlace() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
