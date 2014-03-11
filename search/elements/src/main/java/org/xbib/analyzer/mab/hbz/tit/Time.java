package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class Time extends MABElement {

    private final static MABElement element = new Time();

    private Time() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
