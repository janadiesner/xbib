package org.xbib.analyzer.dublincore

import org.xbib.entities.EntityQueue
import org.xbib.entities.dublincore.DublinCoreEntity

public class IdentifierEntity extends DublinCoreEntity {

    IdentifierEntity build(EntityQueue.EntityWorker worker,  Object key, Object value) {
        println 'got identifier ' + value
        worker.state().getResource().id(value.toString())
        worker.state().getResource().add("dc:identifier", value.toString())
        return this
    }
}
identifier = new IdentifierEntity()
