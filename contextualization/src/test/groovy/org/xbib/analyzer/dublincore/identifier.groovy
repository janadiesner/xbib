package org.xbib.analyzer.dublincore

import org.xbib.entities.EntityBuilder
import org.xbib.entities.dublincore.DublinCoreEntity

public class IdentifierEntity extends DublinCoreEntity {

    @Override
    IdentifierEntity build(EntityBuilder builder, Object key, Object value) {
        println 'got identifier ' + value
        builder.context().getResource().id(value.toString())
        builder.context().getResource().add("dc:identifier", value.toString())
        return this
    }
}
identifierElement = new IdentifierEntity()
