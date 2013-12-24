package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class FormatCarrierDesignator extends MABElement {

    private final static MABElement element = new FormatCarrierDesignator();

    private FormatCarrierDesignator() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
