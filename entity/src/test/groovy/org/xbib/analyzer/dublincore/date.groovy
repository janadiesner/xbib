package org.xbib.analyzer.dublincore

import org.xbib.entities.EntityQueue
import org.xbib.entities.dublincore.DublinCoreEntity

public class DateEntity extends DublinCoreEntity {

    DateEntity build(EntityQueue.EntityWorker worker, Object key, Object value) {
        println 'got date ' + value
        return this
    }
}
date = new DateEntity()
