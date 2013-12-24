package org.xbib.analyzer.marc.bib;

import org.xbib.elements.marc.MARCElement;


public class ItemLibraryIdentifier extends MARCElement {

    private final static MARCElement element = new ItemLibraryIdentifier();

    private ItemLibraryIdentifier() {
    }

    public static MARCElement getInstance() {
        return element;
    }

}
