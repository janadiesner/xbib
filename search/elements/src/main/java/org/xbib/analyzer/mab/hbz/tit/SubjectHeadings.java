package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectHeadings extends MABElement {

    private final static MABElement element = new SubjectHeadings();

    private SubjectHeadings() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
