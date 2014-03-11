package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class TitleUniformIdentifier extends Title {
    
    private final static MABElement element = new TitleUniformIdentifier();

    public static MABElement getInstance() {
        return element;
    }
}
