package org.xbib.rdf.io.xml;

import org.xbib.rdf.RdfContent;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentGenerator;
import org.xbib.rdf.RdfContentParser;
import org.xbib.rdf.StandardRdfContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

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
    public RdfContentGenerator createGenerator(OutputStream os) throws IOException {
        return new XmlContentGenerator(os);
    }

    @Override
    public RdfContentGenerator createGenerator(Writer writer) throws IOException {
        return new XmlContentGenerator(writer);
    }

    @Override
    public RdfContentParser createParser(InputStream in) throws IOException {
        return null;
    }

    @Override
    public RdfContentParser createParser(Reader reader) throws IOException {
        return null;
    }
}
