package org.xbib.analyzer.dublincore
public class CreatorElement extends GroovyDublinCoreElement {
    CreatorElement build(builder, key, value) {
        println 'got author ' + value
        builder.context().getResource().add("dc:creator", value)
        return this
    }
}
creatorElement = new CreatorElement()