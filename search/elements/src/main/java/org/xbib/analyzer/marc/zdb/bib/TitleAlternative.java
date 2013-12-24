package org.xbib.analyzer.marc.zdb.bib;

import org.xbib.elements.marc.MARCElement;

public class TitleAlternative extends MARCElement {
    
    private final static MARCElement element = new TitleAlternative();
    
    private TitleAlternative() {
    }    
    
    public static MARCElement getInstance() {
        return element;
    }
}
