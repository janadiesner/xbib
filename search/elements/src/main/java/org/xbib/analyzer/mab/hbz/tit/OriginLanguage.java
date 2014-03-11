package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class OriginLanguage extends MABElement {

    private final static MABElement element = new OriginLanguage();

    private OriginLanguage() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
