package org.xbib.rdf.io.json;

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

public class JsonContent implements RdfContent {

    public final static JsonContent jsonContent = new JsonContent();

    public static RdfContentBuilder contentBuilder(JsonContentParams params) throws IOException {
        return new RdfContentBuilder(jsonContent, params);
    }

    public static RdfContentBuilder contentBuilder(OutputStream out, JsonContentParams params) throws IOException {
        return new RdfContentBuilder(jsonContent, params, out);
    }

    private JsonContent() {
    }

    @Override
    public StandardRdfContentType type() {
        return null;
    }

    @Override
    public RdfContentGenerator createGenerator(OutputStream out) throws IOException {
        return new JsonContentGenerator(out);
    }

    @Override
    public RdfContentGenerator createGenerator(Writer writer) throws IOException {
        return null;
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
