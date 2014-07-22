package org.xbib.analyzer.output

import org.xbib.rdf.context.ResourceContext
import org.xbib.rdf.context.ResourceContextWriter

public class DublinCoreWriter implements ResourceContextWriter {

    @Override
    void write(ResourceContext resourceContext) throws IOException {
        println 'scripted groovy output, got resource ' + resourceContext.getResource()
    }

    @Override
    void close() throws IOException {

    }

    @Override
    void flush() throws IOException {

    }
}