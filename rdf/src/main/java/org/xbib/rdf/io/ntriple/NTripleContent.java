package org.xbib.rdf.io.ntriple;

import org.xbib.rdf.RdfContent;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentGenerator;
import org.xbib.rdf.RdfContentParser;
import org.xbib.rdf.StandardRdfContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NTripleContent implements RdfContent {

    public final static NTripleContent nTripleContent = new NTripleContent();

    public static RdfContentBuilder contentBuilder(NTripleContentParams params) throws IOException {
        return new RdfContentBuilder(nTripleContent, params);
    }

    public static RdfContentBuilder contentBuilder(OutputStream out, NTripleContentParams params) throws IOException {
        return new RdfContentBuilder(nTripleContent, params, out);
    }

    private NTripleContent() {
    }

    @Override
    public StandardRdfContentType type() {
        return null;
    }

    @Override
    public RdfContentGenerator createGenerator(OutputStream out) throws IOException {
        return new NTripleContentGenerator(out);
    }

    @Override
    public RdfContentParser createParser(InputStream in) throws IOException {
        return new NTripleContentParser(in);
    }
}
