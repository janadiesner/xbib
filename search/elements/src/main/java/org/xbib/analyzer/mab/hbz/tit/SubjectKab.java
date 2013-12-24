package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectKab extends MABElement {
    
    private final static MABElement element = new SubjectKab();
    
    private SubjectKab() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
