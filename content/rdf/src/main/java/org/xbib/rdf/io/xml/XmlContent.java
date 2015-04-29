package org.xbib.rdf.io.xml;

import org.xbib.rdf.RdfContent;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentGenerator;
import org.xbib.rdf.RdfContentParser;
import org.xbib.rdf.StandardRdfContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class XmlContent implements RdfContent {

    public final static XmlContent xmlContent = new XmlContent();

    public static RdfContentBuilder contentBuilder(XmlContentParams params) throws IOException {
        return new RdfContentBuilder(xmlContent, params);
    }

    public static RdfContentBuilder contentBuilder(OutputStream out, XmlContentParams params) throws IOException {
        return new RdfContentBuilder(xmlContent, params, out);
    }

    private XmlContent() {
    }

    @Override
    public StandardRdfContentType type() {
        return StandardRdfContentType.XML;
    }

    @Override
    public RdfContentGenerator createGenerator(OutputStream out) throws IOException {
        return new XmlContentGenerator(out);
    }

    @Override
    public RdfContentParser createParser(InputStream in) throws IOException {
        return new XmlContentParser(in);
    }

}
