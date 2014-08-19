package org.xbib.analyzer

import org.xbib.elements.Element
import org.xbib.elements.ElementBuilder

public class GroovyElement implements Element {
    Element setSettings(Map map) { return this }

    Map<String,Object> getSettings() { return null }

    Element begin() { return this }

    Element build(ElementBuilder builder, Object key, Object value) {
        println 'A default build message from GroovyElement!'
        return this
    }

    Element end() { return this }
}

groovyElement = new GroovyElement()
