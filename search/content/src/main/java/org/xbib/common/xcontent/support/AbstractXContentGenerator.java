
package org.xbib.common.xcontent.support;

import org.xbib.common.xcontent.XContentGenerator;

import java.io.IOException;

public abstract class AbstractXContentGenerator implements XContentGenerator {

    
    public void writeStringField(String fieldName, String value) throws IOException {
        writeFieldName(fieldName);
        writeString(value);
    }

    public void writeBooleanField(String fieldName, boolean value) throws IOException {
        writeFieldName(fieldName);
        writeBoolean(value);
    }

    public void writeNullField(String fieldName) throws IOException {
        writeFieldName(fieldName);
        writeNull();
    }

    public void writeNumberField(String fieldName, int value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    public void writeNumberField(String fieldName, long value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    public void writeNumberField(String fieldName, double value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    public void writeNumberField(String fieldName, float value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    public void writeBinaryField(String fieldName, byte[] data) throws IOException {
        writeFieldName(fieldName);
        writeBinary(data);
    }

    public void writeArrayFieldStart(String fieldName) throws IOException {
        writeFieldName(fieldName);
        writeStartArray();
    }

    public void writeObjectFieldStart(String fieldName) throws IOException {
        writeFieldName(fieldName);
        writeStartObject();
    }
}
