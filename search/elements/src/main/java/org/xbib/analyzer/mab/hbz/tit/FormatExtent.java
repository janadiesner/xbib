package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class FormatExtent extends MABElement {

    private final static MABElement element = new FormatExtent();

    private FormatExtent() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
