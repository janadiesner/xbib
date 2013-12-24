package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectGhbs extends MABElement {
    
    private final static MABElement element = new SubjectGhbs();
    
    private SubjectGhbs() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
