package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class StandardNumber extends MABElement {

    private final static MABElement element = new StandardNumber();

    private StandardNumber() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
