package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class MediaTypeElectronicPreservation extends MABElement {

    private final static MABElement element = new MediaTypeElectronicPreservation();

    private MediaTypeElectronicPreservation() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
