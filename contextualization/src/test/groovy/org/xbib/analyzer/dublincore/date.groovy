package org.xbib.analyzer.dublincore

import org.xbib.entities.EntityBuilder
import org.xbib.entities.dublincore.DublinCoreEntity

public class DateEntity extends DublinCoreEntity {

    @Override
    DateEntity build(EntityBuilder builder, Object key, Object value) {
        println 'got date ' + value
        return this
    }
}
dateElement = new DateEntity()
