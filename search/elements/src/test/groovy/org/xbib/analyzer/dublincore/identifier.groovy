package org.xbib.analyzer.dublincore
public class IdentifierElement extends DublinCoreElement {
    IdentifierElement build(builder, key, value) {
        println 'got identifier ' + value
        builder.context().getResource().id(value)
        builder.context().getResource().add("dc:identifier", value)
        return this
    }
}
identifierElement = new IdentifierElement()
