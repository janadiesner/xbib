package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class RecordIdentifierSuper extends MABElement {
    
    private final static MABElement element = new RecordIdentifierSuper();
    
    private RecordIdentifierSuper() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
