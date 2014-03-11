package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class FormatComputer extends MABElement {

    private final static MABElement element = new FormatComputer();

    private FormatComputer() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
