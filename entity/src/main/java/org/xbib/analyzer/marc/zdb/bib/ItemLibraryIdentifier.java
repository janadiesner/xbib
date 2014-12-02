package org.xbib.analyzer.marc.zdb.bib;

import org.xbib.entities.marc.MARCEntityQueue;
import org.xbib.entities.support.ValueMaps;
import org.xbib.entities.marc.MARCEntity;
import org.xbib.rdf.Resource;

import java.util.List;
import java.util.Map;

/**
 * TODO replace by library service onotology
 */
public class ItemLibraryIdentifier extends MARCEntity {

    private final static ItemLibraryIdentifier element = new ItemLibraryIdentifier();

    private final static Map<String, String> sigel2isil =
            ValueMaps.getAssocStringMap("sigel2isil");

    private final static Map<String, Map<String, List<String>>> product2isil =
            ValueMaps.getMap("product2isil");

    private ItemLibraryIdentifier() {
    }

    public static ItemLibraryIdentifier getInstance() {
        return element;
    }


    @Override
    public String data(MARCEntityQueue.MARCWorker worker,
                       String predicate, Resource resource, String property, String value) {
        if (value == null) {
            return null;
        }
        if ("identifier".equals(property)) {
            return resolveIdentifier(value);
        }
        return value;
    }

    private String resolveIdentifier(String value) {
        if (product2isil.containsKey(value)) {
            for (String isil : product2isil.get(value).get("authorized")) {
                //createISIL(b, isil, value);
            }
            //createISIL(b, value, null);
        }
        return sigel2isil.get(value);
    }
    /*public ItemLibraryIdentifier build(MARCEntityBuilder b, FieldList key, String value) {
        boolean servicecreated = false;
        for (Field d : key) {
            String s = d.subfieldId();
            if (s.equals("a")) {
                resolveIdentifier(b, d.data());

            } else if (s.equals("e")) {
                createItemService(b, d.data());
                servicecreated = true;

            }
        }
        if (!servicecreated) {
            createItemService(b, null);            
        }
        return this;
    }*/


}
