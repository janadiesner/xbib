
package org.xbib.common.xcontent.yaml;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import org.xbib.common.io.BytesReference;
import org.xbib.common.xcontent.XContentType;
import org.xbib.common.xcontent.json.JsonXContentGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class YamlXContentGenerator extends JsonXContentGenerator {

    public YamlXContentGenerator(JsonGenerator generator) {
        super(generator);
    }

    
    public XContentType contentType() {
        return XContentType.YAML;
    }

    
    public void writeRawField(String fieldName, InputStream content, OutputStream bos) throws IOException {
        writeFieldName(fieldName);
        YAMLParser parser = YamlXContent.yamlFactory.createJsonParser(content);
        try {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        } finally {
            parser.close();
        }
    }

    
    public void writeRawField(String fieldName, byte[] content, OutputStream bos) throws IOException {
        writeFieldName(fieldName);
        YAMLParser parser = YamlXContent.yamlFactory.createJsonParser(content);
        try {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        } finally {
            parser.close();
        }
    }

    
    public void writeRawField(String fieldName, BytesReference content, OutputStream bos) throws IOException {
        writeFieldName(fieldName);
        YAMLParser parser;
        if (content.hasArray()) {
            parser = YamlXContent.yamlFactory.createJsonParser(content.array(), content.arrayOffset(), content.length());
        } else {
            parser = YamlXContent.yamlFactory.createJsonParser(content.streamInput());
        }
        try {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        } finally {
            parser.close();
        }
    }

    
    public void writeRawField(String fieldName, byte[] content, int offset, int length, OutputStream bos) throws IOException {
        writeFieldName(fieldName);
        YAMLParser parser = YamlXContent.yamlFactory.createJsonParser(content, offset, length);
        try {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        } finally {
            parser.close();
        }
    }
}
