package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class OtherPublicationStatements extends MABElement {

    private final static MABElement element = new OtherPublicationStatements();

    private OtherPublicationStatements() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
