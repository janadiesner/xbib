package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectBay extends MABElement {
    
    private final static MABElement element = new SubjectBay();
    
    private SubjectBay() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
