
package org.xbib.common.xcontent.smile;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.dataformat.smile.SmileGenerator;
import org.xbib.io.BytesReference;
import org.xbib.io.FastStringReader;
import org.xbib.common.xcontent.XContent;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.common.xcontent.XContentGenerator;
import org.xbib.common.xcontent.XContentParser;
import org.xbib.common.xcontent.XContentType;
import org.xbib.common.xcontent.json.JsonXContentParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;


/**
 * A JSON based content implementation using Jackson.
 */
public class SmileXContent implements XContent {

    public static XContentBuilder contentBuilder() throws IOException {
        return XContentBuilder.builder(smileXContent);
    }

    final static SmileFactory smileFactory;
    public final static SmileXContent smileXContent;

    static {
        smileFactory = new SmileFactory();
        smileFactory.configure(SmileGenerator.Feature.ENCODE_BINARY_AS_7BIT, false); // for now, this is an overhead, might make sense for web sockets
        smileXContent = new SmileXContent();
    }

    private SmileXContent() {
    }

    
    public XContentType type() {
        return XContentType.SMILE;
    }

    
    public byte streamSeparator() {
        return (byte) 0xFF;
    }

    
    public XContentGenerator createGenerator(OutputStream os) throws IOException {
        return new SmileXContentGenerator(smileFactory.createGenerator(os, JsonEncoding.UTF8));
    }

    
    public XContentGenerator createGenerator(Writer writer) throws IOException {
        return new SmileXContentGenerator(smileFactory.createGenerator(writer));
    }

    
    public XContentParser createParser(String content) throws IOException {
        return new SmileXContentParser(smileFactory.createParser(new FastStringReader(content)));
    }

    
    public XContentParser createParser(InputStream is) throws IOException {
        return new SmileXContentParser(smileFactory.createParser(is));
    }

    
    public XContentParser createParser(byte[] data) throws IOException {
        return new SmileXContentParser(smileFactory.createParser(data));
    }

    
    public XContentParser createParser(byte[] data, int offset, int length) throws IOException {
        return new SmileXContentParser(smileFactory.createParser(data, offset, length));
    }

    
    public XContentParser createParser(BytesReference bytes) throws IOException {
        if (bytes.hasArray()) {
            return createParser(bytes.array(), bytes.arrayOffset(), bytes.length());
        }
        return createParser(bytes.streamInput());
    }

    
    public XContentParser createParser(Reader reader) throws IOException {
        return new JsonXContentParser(smileFactory.createParser(reader));
    }
}
