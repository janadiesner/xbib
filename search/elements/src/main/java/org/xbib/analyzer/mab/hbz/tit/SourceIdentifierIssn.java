package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SourceIdentifierIssn extends MABElement {
    
    private final static MABElement element = new SourceIdentifierIssn();
    
    private SourceIdentifierIssn() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
