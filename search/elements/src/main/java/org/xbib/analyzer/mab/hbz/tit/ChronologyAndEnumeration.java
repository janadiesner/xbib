package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class ChronologyAndEnumeration extends MABElement {

    private final static MABElement element = new ChronologyAndEnumeration();

    private ChronologyAndEnumeration() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
