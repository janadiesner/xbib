package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class IdentifierLocal extends MABElement {

    private final static MABElement element = new IdentifierLocal();

    private IdentifierLocal() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
