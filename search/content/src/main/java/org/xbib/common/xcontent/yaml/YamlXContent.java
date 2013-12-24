
package org.xbib.common.xcontent.yaml;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.xbib.common.io.BytesReference;
import org.xbib.common.io.FastStringReader;
import org.xbib.common.xcontent.XContent;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.common.xcontent.XContentGenerator;
import org.xbib.common.xcontent.XContentParser;
import org.xbib.common.xcontent.XContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;


/**
 * A YAML based content implementation using Jackson.
 */
public class YamlXContent implements XContent {

    public static XContentBuilder contentBuilder() throws IOException {
        return XContentBuilder.builder(yamlXContent);
    }

    final static YAMLFactory yamlFactory;
    public final static YamlXContent yamlXContent;

    static {
        yamlFactory = new YAMLFactory();
        yamlXContent = new YamlXContent();
    }

    private YamlXContent() {
    }

    
    public XContentType type() {
        return XContentType.YAML;
    }

    
    public byte streamSeparator() {
        throw new UnsupportedOperationException("yaml does not support stream parsing...");
    }

    
    public XContentGenerator createGenerator(OutputStream os) throws IOException {
        return new YamlXContentGenerator(yamlFactory.createGenerator(os, JsonEncoding.UTF8));
    }

    
    public XContentGenerator createGenerator(Writer writer) throws IOException {
        return new YamlXContentGenerator(yamlFactory.createGenerator(writer));
    }

    
    public XContentParser createParser(String content) throws IOException {
        return new YamlXContentParser(yamlFactory.createParser(new FastStringReader(content)));
    }

    
    public XContentParser createParser(InputStream is) throws IOException {
        return new YamlXContentParser(yamlFactory.createParser(is));
    }

    
    public XContentParser createParser(byte[] data) throws IOException {
        return new YamlXContentParser(yamlFactory.createParser(data));
    }

    
    public XContentParser createParser(byte[] data, int offset, int length) throws IOException {
        return new YamlXContentParser(yamlFactory.createParser(data, offset, length));
    }

    
    public XContentParser createParser(BytesReference bytes) throws IOException {
        if (bytes.hasArray()) {
            return createParser(bytes.array(), bytes.arrayOffset(), bytes.length());
        }
        return createParser(bytes.streamInput());
    }

    
    public XContentParser createParser(Reader reader) throws IOException {
        return new YamlXContentParser(yamlFactory.createParser(reader));
    }
}
