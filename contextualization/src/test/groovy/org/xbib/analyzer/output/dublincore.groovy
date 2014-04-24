package org.xbib.analyzer.output

import org.xbib.analyzer.dublincore.DublinCoreContext
import org.xbib.elements.context.CountableContextResourceOutput
import org.xbib.rdf.Resource
import org.xbib.rdf.content.ContentBuilder

public class DublinCoreOutput extends CountableContextResourceOutput<DublinCoreContext, Resource> {
    public void output(DublinCoreContext context, Resource resource, ContentBuilder builder) {
         println 'scripted groovy output, got resource ' + resource
    }
}