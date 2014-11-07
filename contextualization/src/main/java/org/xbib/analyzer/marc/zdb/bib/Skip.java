package org.xbib.analyzer.marc.zdb.bib;

import org.xbib.elements.marc.MARCElement;
import org.xbib.elements.marc.MARCElementBuilder;
import org.xbib.marc.FieldList;

public class Skip extends MARCElement {
    
    private final static MARCElement element = new Skip();
    
    private Skip() {
    }
        
    public static MARCElement getInstance() {
        return element;
    }

    @Override
    public Skip build(MARCElementBuilder b, FieldList key, String value) {
        return this;
    }

}
