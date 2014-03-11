package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class OtherExtents extends MABElement {

    private final static MABElement element = new OtherExtents();

    private OtherExtents() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
