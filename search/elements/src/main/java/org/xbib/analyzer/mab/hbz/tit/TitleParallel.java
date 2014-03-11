package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class TitleParallel extends Title {

    private final static MABElement element = new TitleParallel();

    private TitleParallel() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
