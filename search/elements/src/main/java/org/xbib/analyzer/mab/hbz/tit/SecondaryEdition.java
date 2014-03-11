package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SecondaryEdition extends MABElement {

    private final static MABElement element = new SecondaryEdition();

    private SecondaryEdition() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
