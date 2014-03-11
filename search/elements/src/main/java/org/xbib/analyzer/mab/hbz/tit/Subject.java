package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class Subject extends MABElement {

    private final static MABElement element = new Subject();

    private Subject() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
