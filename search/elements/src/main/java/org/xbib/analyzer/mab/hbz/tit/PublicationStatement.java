package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class PublicationStatement extends MABElement {

    private final static MABElement element = new PublicationStatement();

    private PublicationStatement() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
