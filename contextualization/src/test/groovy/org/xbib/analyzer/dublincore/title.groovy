package org.xbib.analyzer.dublincore

import org.xbib.elements.ElementBuilder;

public class TitleElement extends DublinCoreElement {
    @Override
    TitleElement build(ElementBuilder builder, Object key, Object value) {
        println 'got title ' + value
        builder.context().getResource().add("dc:title", value.toString())
        return this
    }
}
titleElement = new TitleElement()
