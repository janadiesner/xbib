package org.xbib.analyzer.marc.zdb.bib;

import org.xbib.entities.marc.MARCEntityQueue;
import org.xbib.entities.marc.MARCEntity;
import org.xbib.rdf.Resource;

public class ItemLibraryIdentifier extends MARCEntity {

    private final static ItemLibraryIdentifier element = new ItemLibraryIdentifier();

    private ItemLibraryIdentifier() {
    }

    public static ItemLibraryIdentifier getInstance() {
        return element;
    }


    @Override
    public String data(MARCEntityQueue.MARCWorker worker,
                       String predicate, Resource resource, String property, String value) {
        return value;
    }

}
