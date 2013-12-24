package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectBk extends MABElement {
    
    private final static MABElement element = new SubjectBk();
    
    private SubjectBk() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
