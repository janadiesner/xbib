package org.xbib.analyzer.mab.hbz.tit;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class RecordIdentifier extends MABElement {
    
    private final static MABElement element = new RecordIdentifier();
    
    private RecordIdentifier() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    /*@Override
    public void fields(ElementBuilder<FieldCollection, String, MABElement, MABContext> builder,
                       FieldCollection fields, String value) {
        //builder.context().resource().id(IRI.builder().scheme("mab").host(value.trim()).build());
    }*/
}
