
package org.xbib.common.xcontent.json;

import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.xbib.io.BytesReference;
import org.xbib.common.xcontent.XContentGenerator;
import org.xbib.common.xcontent.XContentHelper;
import org.xbib.common.xcontent.XContentParser;
import org.xbib.common.xcontent.XContentType;

public class JsonXContentGenerator implements XContentGenerator {

    protected final JsonGenerator generator;

    public JsonXContentGenerator(JsonGenerator generator) {
        this.generator = generator;
    }

    
    public XContentType contentType() {
        return XContentType.JSON;
    }

    
    public void usePrettyPrint() {
        generator.useDefaultPrettyPrinter();
    }

    
    public void writeStartArray() throws IOException {
        generator.writeStartArray();
    }

    
    public void writeEndArray() throws IOException {
        generator.writeEndArray();
    }

    
    public void writeStartObject() throws IOException {
        generator.writeStartObject();
    }

    
    public void writeEndObject() throws IOException {
        generator.writeEndObject();
    }

    
    public void writeFieldName(String name) throws IOException {
        generator.writeFieldName(name);
    }
    
    public void writeString(String text) throws IOException {
        generator.writeString(text);
    }

    
    public void writeString(char[] text, int offset, int len) throws IOException {
        generator.writeString(text, offset, len);
    }

    
    public void writeUTF8String(byte[] text, int offset, int length) throws IOException {
        generator.writeUTF8String(text, offset, length);
    }

    
    public void writeBinary(byte[] data, int offset, int len) throws IOException {
        generator.writeBinary(data, offset, len);
    }

    
    public void writeBinary(byte[] data) throws IOException {
        generator.writeBinary(data);
    }

    
    public void writeNumber(int v) throws IOException {
        generator.writeNumber(v);
    }

    
    public void writeNumber(long v) throws IOException {
        generator.writeNumber(v);
    }

    
    public void writeNumber(double d) throws IOException {
        generator.writeNumber(d);
    }

    
    public void writeNumber(float f) throws IOException {
        generator.writeNumber(f);
    }

    
    public void writeBoolean(boolean state) throws IOException {
        generator.writeBoolean(state);
    }

    
    public void writeNull() throws IOException {
        generator.writeNull();
    }

    
    public void writeStringField(String fieldName, String value) throws IOException {
        generator.writeStringField(fieldName, value);
    }

    
    public void writeBooleanField(String fieldName, boolean value) throws IOException {
        generator.writeBooleanField(fieldName, value);
    }

    
    public void writeNullField(String fieldName) throws IOException {
        generator.writeNullField(fieldName);
    }

    public void writeNumberField(String fieldName, int value) throws IOException {
        generator.writeNumberField(fieldName, value);
    }

    public void writeNumberField(String fieldName, long value) throws IOException {
        generator.writeNumberField(fieldName, value);
    }

    public void writeNumberField(String fieldName, double value) throws IOException {
        generator.writeNumberField(fieldName, value);
    }

    public void writeNumberField(String fieldName, float value) throws IOException {
        generator.writeNumberField(fieldName, value);
    }

    public void writeBinaryField(String fieldName, byte[] data) throws IOException {
        generator.writeBinaryField(fieldName, data);
    }
    
    public void writeArrayFieldStart(String fieldName) throws IOException {
        generator.writeArrayFieldStart(fieldName);
    }


    public void writeObjectFieldStart(String fieldName) throws IOException {
        generator.writeObjectFieldStart(fieldName);
    }

    
    public void writeRawField(String fieldName, byte[] content, OutputStream bos) throws IOException {
        generator.writeRaw(", \"");
        generator.writeRaw(fieldName);
        generator.writeRaw("\" : ");
        flush();
        bos.write(content);
    }

    
    public void writeRawField(String fieldName, byte[] content, int offset, int length, OutputStream bos) throws IOException {
        generator.writeRaw(", \"");
        generator.writeRaw(fieldName);
        generator.writeRaw("\" : ");
        flush();
        bos.write(content, offset, length);
    }

    
    public void writeRawField(String fieldName, InputStream content, OutputStream bos) throws IOException {
        generator.writeRaw(", \"");
        generator.writeRaw(fieldName);
        generator.writeRaw("\" : ");
        flush();
        copy(content, bos);
    }

    public static final int BUFFER_SIZE = 1024 * 8;

    public static long copy(InputStream in, OutputStream out) throws IOException {
        return copy(in, out, new byte[BUFFER_SIZE]);
    }

    public static long copy(InputStream in, OutputStream out, byte[] buffer) throws IOException {
        try {
            long byteCount = 0;
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                byteCount += bytesRead;
            }
            out.flush();
            return byteCount;
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                // do nothing
            }
            try {
                out.close();
            } catch (IOException ex) {
                // do nothing
            }
        }
    }
    
    public void writeRawField(String fieldName, BytesReference content, OutputStream bos) throws IOException {
        generator.writeRaw(", \"");
        generator.writeRaw(fieldName);
        generator.writeRaw("\" : ");
        flush();
        content.writeTo(bos);
    }

    
    public void copyCurrentStructure(XContentParser parser) throws IOException {
        // the start of the parser
        if (parser.currentToken() == null) {
            parser.nextToken();
        }
        if (parser instanceof JsonXContentParser) {
            generator.copyCurrentStructure(((JsonXContentParser) parser).parser);
        } else {
            XContentHelper.copyCurrentStructure(this, parser);
        }
    }

    
    public void flush() throws IOException {
        generator.flush();
    }

    
    public void close() throws IOException {
        generator.close();
    }
}
