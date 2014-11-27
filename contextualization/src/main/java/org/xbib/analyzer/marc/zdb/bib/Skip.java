package org.xbib.analyzer.marc.zdb.bib;

import org.xbib.entities.marc.MARCEntity;

public class Skip extends MARCEntity {
    
    private final static Skip element = new Skip();
        
    public static Skip getInstance() {
        return element;
    }

}
