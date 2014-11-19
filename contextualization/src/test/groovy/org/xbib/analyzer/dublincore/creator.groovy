package org.xbib.analyzer.dublincore

import org.xbib.entities.Entity
import org.xbib.entities.EntityBuilder

public class CreatorEntity extends GroovyDublinCoreEntity {

    @Override
    Entity build(EntityBuilder builder, Object key, Object value) {
        println 'got author ' + value
        builder.context().getResource().add("dc:creator", value.toString())
        return this
    }
}

creatorElement = new CreatorEntity()