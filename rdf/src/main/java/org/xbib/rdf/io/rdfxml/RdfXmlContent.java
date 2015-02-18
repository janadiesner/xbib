package org.xbib.rdf.io.rdfxml;

import org.xbib.rdf.RdfContent;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentGenerator;
import org.xbib.rdf.RdfContentParser;
import org.xbib.rdf.StandardRdfContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RdfXmlContent implements RdfContent {

    public final static RdfXmlContent rdfXmlContent = new RdfXmlContent();

    public static RdfContentBuilder contentBuilder(RdfXmlContentParams params) throws IOException {
        return new RdfContentBuilder(rdfXmlContent, params);
    }

    public static RdfContentBuilder contentBuilder(OutputStream out, RdfXmlContentParams params) throws IOException {
        return new RdfContentBuilder(rdfXmlContent, params, out);
    }

    private RdfXmlContent() {
    }

    @Override
    public StandardRdfContentType type() {
        return StandardRdfContentType.RDFXML;
    }

    @Override
    public RdfContentGenerator createGenerator(OutputStream os) throws IOException {
        return new RdfXmlContentGenerator(os);
    }

    @Override
    public RdfContentParser createParser(InputStream in) throws IOException {
        return new RdfXmlContentParser(in);
    }
}
