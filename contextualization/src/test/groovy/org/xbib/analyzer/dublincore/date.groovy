package org.xbib.analyzer.dublincore

import org.xbib.elements.ElementBuilder

public class DateElement extends DublinCoreElement {

    @Override
    DateElement build(ElementBuilder builder, Object key, Object value) {
        println 'got date ' + value
        return this
    }
}
dateElement = new DateElement()
