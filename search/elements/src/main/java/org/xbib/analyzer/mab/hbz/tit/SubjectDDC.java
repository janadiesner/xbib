package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectDDC extends MABElement {

    private final static MABElement element = new SubjectDDC();

    private SubjectDDC() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
