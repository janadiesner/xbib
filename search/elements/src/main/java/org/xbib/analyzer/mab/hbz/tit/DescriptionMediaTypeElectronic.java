package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class DescriptionMediaTypeElectronic extends MABElement {

    private final static MABElement element = new DescriptionMediaTypeElectronic();

    private DescriptionMediaTypeElectronic() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
