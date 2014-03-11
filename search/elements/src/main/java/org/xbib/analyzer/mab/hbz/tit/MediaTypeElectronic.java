package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class MediaTypeElectronic extends MABElement {
    
    private final static MABElement element = new MediaTypeElectronic();
    
    private MediaTypeElectronic() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
