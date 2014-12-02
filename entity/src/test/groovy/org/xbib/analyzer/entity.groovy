package org.xbib.analyzer

import org.xbib.entities.Entity

public class GroovyEntity implements Entity {

    Map settings;

    Entity setSettings(Map map) {
        this.settings = map;
        return this
    }

    Entity build(Object key, Object value) {
        println 'A default complete message from GroovyEntity!'
        return this
    }

}

groovyEntity = new entity()
