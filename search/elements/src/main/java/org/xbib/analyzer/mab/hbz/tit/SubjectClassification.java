package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectClassification extends MABElement {

    private final static MABElement element = new SubjectClassification();

    private SubjectClassification() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
