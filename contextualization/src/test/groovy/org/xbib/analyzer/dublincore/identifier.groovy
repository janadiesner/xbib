package org.xbib.analyzer.dublincore

import org.xbib.elements.ElementBuilder

public class IdentifierElement extends DublinCoreElement {

    @Override
    IdentifierElement build(ElementBuilder builder, key, value) {
        println 'got identifier ' + value
        builder.context().getResource().id(value.toString())
        builder.context().getResource().add("dc:identifier", value.toString())
        return this
    }
}
identifierElement = new IdentifierElement()
