package org.xbib.analyzer.dublincore

import org.xbib.entities.Entity
import org.xbib.entities.EntityQueue

public class CreatorEntity extends GroovyDublinCoreEntity {

    Entity build(EntityQueue.EntityWorker worker, Object key, Object value) {
        println 'got author ' + value
        worker.state().getResource().add("dc:creator", value.toString())
        return this
    }
}

creator = new creator()