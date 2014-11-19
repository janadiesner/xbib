package org.xbib.analyzer.dublincore

import org.xbib.entities.EntityBuilder
import org.xbib.entities.dublincore.DublinCoreEntity;

public class TitleEntity extends DublinCoreEntity {
    @Override
    TitleEntity build(EntityBuilder builder, Object key, Object value) {
        println 'got title ' + value
        builder.context().getResource().add("dc:title", value.toString())
        return this
    }
}
titleElement = new TitleEntity()
