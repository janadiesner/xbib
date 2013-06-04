package org.xbib.analyzer.elements.mab;

import org.xbib.elements.marc.extensions.mab.MABBuilder;
import org.xbib.elements.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;


public class CjkPublisherPlace extends MABElement {
    
    private final static MABElement element = new CjkPublisherPlace();
    
    private CjkPublisherPlace() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
