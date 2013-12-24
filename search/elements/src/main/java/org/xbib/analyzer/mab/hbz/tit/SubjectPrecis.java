package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectPrecis extends MABElement {
    
    private final static MABElement element = new SubjectPrecis();
    
    private SubjectPrecis() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
