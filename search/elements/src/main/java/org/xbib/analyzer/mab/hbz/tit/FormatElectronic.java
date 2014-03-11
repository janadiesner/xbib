package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class FormatElectronic extends MABElement {

    private final static MABElement element = new FormatElectronic();

    private FormatElectronic() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
