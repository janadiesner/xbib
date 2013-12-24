package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class TitleRelated extends Title {

    private final static MABElement element = new TitleRelated();

    private TitleRelated() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
