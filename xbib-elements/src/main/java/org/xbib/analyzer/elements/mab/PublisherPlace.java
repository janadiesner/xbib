package org.xbib.analyzer.elements.mab;

import org.xbib.analyzer.marc.extensions.mab.MABBuilder;
import org.xbib.analyzer.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;


public class PublisherPlace extends MABElement {
    
    private final static MABElement element = new PublisherPlace();
    
    private PublisherPlace() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public PublisherPlace build(MABBuilder b, FieldCollection key, String value) {
        // b.context().getResource(b.context().resource(), ...).add( ... , value);
        return this;
    }

}
