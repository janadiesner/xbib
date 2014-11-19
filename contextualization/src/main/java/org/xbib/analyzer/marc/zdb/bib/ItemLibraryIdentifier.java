package org.xbib.analyzer.marc.zdb.bib;

import org.xbib.entities.ValueMapFactory;
import org.xbib.entities.marc.MARCEntity;
import org.xbib.entities.marc.MARCEntityBuilder;
import org.xbib.marc.FieldList;
import org.xbib.marc.Field;

import java.util.List;
import java.util.Map;

/**
 * TODO replace by library service onotology
 */
public class ItemLibraryIdentifier extends MARCEntity {

    private final static ItemLibraryIdentifier element = new ItemLibraryIdentifier();

    private final static Map<String, String> sigel2isil =
            ValueMapFactory.getAssocStringMap("sigel2isil");

    private final static Map<String, Map<String, List<String>>> product2isil =
            ValueMapFactory.getMap("product2isil");

    private ItemLibraryIdentifier() {
    }

    public static ItemLibraryIdentifier getInstance() {
        return element;
    }

    @Override
    public ItemLibraryIdentifier build(MARCEntityBuilder b, FieldList key, String value) {
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
    }

    private String resolveIdentifier(MARCEntityBuilder b, String value) {
        if (product2isil.containsKey(value)) {
            for (String isil : product2isil.get(value).get("authorized")) {
                createISIL(b, isil, value);
            }
            createISIL(b, value, null);
        }
        String isil = sigel2isil.get(value);
        if (isil != null) {
            createISIL(b, isil, null);
        }
        return isil;
    }

    private void createISIL(MARCEntityBuilder b, String isil, String provider) {
    }

    private void createItemService(MARCEntityBuilder b, String itemStatus) {
    }
}
