
package org.xbib.common.xcontent.json;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.xbib.io.BytesReference;
import org.xbib.io.FastStringReader;
import org.xbib.common.xcontent.XContent;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.common.xcontent.XContentGenerator;
import org.xbib.common.xcontent.XContentParser;
import org.xbib.common.xcontent.XContentType;


/**
 * A JSON based content implementation using Jackson.
 */
public class JsonXContent implements XContent {

    public static XContentBuilder contentBuilder() throws IOException {
        return XContentBuilder.builder(jsonXContent);
    }

    private final static JsonFactory jsonFactory;
    public final static JsonXContent jsonXContent;

    static {
        jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        jsonFactory.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
        jsonXContent = new JsonXContent();
    }

    private JsonXContent() {
    }

    
    public XContentType type() {
        return XContentType.JSON;
    }

    
    public byte streamSeparator() {
        return '\n';
    }

    
    public XContentGenerator createGenerator(OutputStream os) throws IOException {
        return new JsonXContentGenerator(jsonFactory.createGenerator(os, JsonEncoding.UTF8));
    }

    
    public XContentGenerator createGenerator(Writer writer) throws IOException {
        return new JsonXContentGenerator(jsonFactory.createGenerator(writer));
    }

    
    public XContentParser createParser(String content) throws IOException {
        return new JsonXContentParser(jsonFactory.createParser(new FastStringReader(content)));
    }

    
    public XContentParser createParser(InputStream is) throws IOException {
        return new JsonXContentParser(jsonFactory.createParser(is));
    }

    
    public XContentParser createParser(byte[] data) throws IOException {
        return new JsonXContentParser(jsonFactory.createParser(data));
    }

    
    public XContentParser createParser(byte[] data, int offset, int length) throws IOException {
        return new JsonXContentParser(jsonFactory.createParser(data, offset, length));
    }

    
    public XContentParser createParser(BytesReference bytes) throws IOException {
        if (bytes.hasArray()) {
            return createParser(bytes.array(), bytes.arrayOffset(), bytes.length());
        }
        return createParser(bytes.streamInput());
    }

    
    public XContentParser createParser(Reader reader) throws IOException {
        return new JsonXContentParser(jsonFactory.createParser(reader));
    }
}
