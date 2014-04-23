package org.xbib.analyzer.output
import org.xbib.analyzer.dublincore.DublinCoreContext
import org.xbib.elements.context.CountableContextResourceOutput
import org.xbib.rdf.Resource
import org.xbib.rdf.xcontent.ContentBuilder

public class DublinCoreOutput extends CountableContextResourceOutput<DublinCoreContext, Resource> {
    public void output(DublinCoreContext context, ContentBuilder builder) {
         println 'scripted output, got getResource ' + context.getResource()
         return
    }
    public long getCounter() {
        return 0;
    }
}