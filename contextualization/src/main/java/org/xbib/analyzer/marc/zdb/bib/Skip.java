package org.xbib.analyzer.marc.zdb.bib;

import org.xbib.entities.marc.MARCEntity;
import org.xbib.entities.marc.MARCEntityBuilder;
import org.xbib.marc.FieldList;

public class Skip extends MARCEntity {
    
    private final static Skip element = new Skip();
        
    public static Skip getInstance() {
        return element;
    }

    @Override
    public Skip build(MARCEntityBuilder b, FieldList key, String value) {
        return this;
    }

}
