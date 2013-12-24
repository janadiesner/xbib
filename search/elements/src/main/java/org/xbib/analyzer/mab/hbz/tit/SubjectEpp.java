package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectEpp extends MABElement {
    
    private final static MABElement element = new SubjectEpp();
    
    private SubjectEpp() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
