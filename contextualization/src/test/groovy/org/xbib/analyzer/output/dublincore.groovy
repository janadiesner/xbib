package org.xbib.analyzer.output

import org.xbib.rdf.Context
import org.xbib.rdf.ResourceWriter

public class DublinCoreWriter implements ResourceWriter {

    @Override
    void write(Context resourceContext) throws IOException {
        println 'scripted groovy output'
    }

    @Override
    void close() throws IOException {

    }

    @Override
    void flush() throws IOException {

    }
}