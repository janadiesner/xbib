package org.xbib.analyzer.dublincore

import org.xbib.elements.ElementBuilder

public class CreatorElement extends GroovyDublinCoreElement {

    @Override
    CreatorElement build(ElementBuilder builder, key, value) {
        println 'got author ' + value
        builder.context().getResource().add("dc:creator", value.toString())
        return this
    }
}

creatorElement = new CreatorElement()