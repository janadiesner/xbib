package org.xbib.analyzer.elements.mab;

import org.xbib.analyzer.marc.extensions.mab.MABBuilder;
import org.xbib.analyzer.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;


public class SubjectAsb extends MABElement {
    
    private final static MABElement element = new SubjectAsb();
    
    private SubjectAsb() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public SubjectAsb build(MABBuilder b, FieldCollection key, String value) {
        // b.context().getResource(b.context().resource(), ...).add( ... , value);
        return this;
    }

}
