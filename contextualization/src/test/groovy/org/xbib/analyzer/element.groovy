package org.xbib.analyzer

import org.xbib.entities.Entity
import org.xbib.entities.EntityBuilder

public class GroovyEntity implements Entity {
    Entity setSettings(Map map) { return this }

    Map<String,Object> getSettings() { return null }

    Entity begin() { return this }

    Entity build(EntityBuilder builder, Object key, Object value) {
        println 'A default complete message from GroovyElement!'
        return this
    }

    Entity end() { return this }
}

groovyElement = new GroovyEntity()
