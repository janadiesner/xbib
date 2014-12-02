package org.xbib.analyzer.dublincore

import org.xbib.entities.EntityQueue
import org.xbib.entities.dublincore.DublinCoreEntity;

public class TitleEntity extends DublinCoreEntity {

    TitleEntity build(EntityQueue.EntityWorker worker, Object key, Object value) {
        println 'got title ' + value
        worker.state().getResource().add("dc:title", value.toString())
        return this
    }
}
title = new TitleEntity()
