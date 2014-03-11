
package org.xbib.common.xcontent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.xbib.io.BytesArray;
import org.xbib.io.BytesReference;
import org.xbib.io.stream.BytesStream;
import org.xbib.io.FastByteArrayOutputStream;
import org.xbib.common.xcontent.support.XContentMapConverter;

public final class XContentBuilder implements BytesStream {

    public static enum FieldCaseConversion {
        /**
         * No came conversion will occur.
         */
        NONE,
        /**
         * Camel Case will be converted to Underscore casing.
         */
        UNDERSCORE,
        /**
         * Underscore will be converted to Camel case conversion.
         */
        CAMELCASE
    }

    protected static FieldCaseConversion globalFieldCaseConversion = FieldCaseConversion.NONE;

    public static void globalFieldCaseConversion(FieldCaseConversion globalFieldCaseConversion) {
        XContentBuilder.globalFieldCaseConversion = globalFieldCaseConversion;
    }

    /**
     * Constructs a new builder using a fresh {@link FastByteArrayOutputStream}.
     */
    public static XContentBuilder builder(XContent xContent) throws IOException {
        return new XContentBuilder(xContent, new FastByteArrayOutputStream());
    }

    public static XContentBuilder builder(XContent xContent, Object payload) throws IOException {
        return new XContentBuilder(xContent, new FastByteArrayOutputStream(), payload);
    }

    private XContentGenerator generator;

    private final OutputStream bos;

    private final Object payload;

    private FieldCaseConversion fieldCaseConversion = globalFieldCaseConversion;

    private StringBuilder cachedStringBuilder;

    /**
     * Constructs a new builder using the provided xcontent and an OutputStream. Make sure
     * to call {@link #close()} when the builder is done with.
     */
    public XContentBuilder(XContent xContent, OutputStream bos) throws IOException {
        this(xContent, bos, null);
    }

    /**
     * Constructs a new builder using the provided xcontent and an OutputStream. Make sure
     * to call {@link #close()} when the builder is done with.
     */
    public XContentBuilder(XContent xContent, OutputStream bos, Object payload) throws IOException {
        this.bos = bos;
        this.generator = xContent.createGenerator(bos);
        this.payload = payload;
    }

    public XContentBuilder fieldCaseConversion(FieldCaseConversion fieldCaseConversion) {
        this.fieldCaseConversion = fieldCaseConversion;
        return this;
    }

    public XContentType contentType() {
        return generator.contentType();
    }

    public XContentGenerator generator() {
        return generator;
    }

    public XContentBuilder prettyPrint() {
        generator.usePrettyPrint();
        return this;
    }

    public XContentBuilder field(String name, ToXContent xContent) throws IOException {
        field(name);
        xContent.toXContent(this, ToXContent.EMPTY_PARAMS);
        return this;
    }

    public XContentBuilder field(String name, ToXContent xContent, ToXContent.Params params) throws IOException {
        field(name);
        xContent.toXContent(this, params);
        return this;
    }

    public XContentBuilder startObject(String name) throws IOException {
        field(name);
        startObject();
        return this;
    }

    public XContentBuilder startObject(String name, FieldCaseConversion conversion) throws IOException {
        field(name, conversion);
        startObject();
        return this;
    }

    public XContentBuilder startObject() throws IOException {
        generator.writeStartObject();
        return this;
    }

    public XContentBuilder endObject() throws IOException {
        generator.writeEndObject();
        return this;
    }

    public XContentBuilder array(String name, Collection values) throws IOException {
        startArray(name);
        for (Object value : values) {
            value(value);
        }
        endArray();
        return this;
    }

    public XContentBuilder array(String name, String... values) throws IOException {
        startArray(name);
        for (String value : values) {
            value(value);
        }
        endArray();
        return this;
    }

    public XContentBuilder array(String name, Object... values) throws IOException {
        startArray(name);
        for (Object value : values) {
            value(value);
        }
        endArray();
        return this;
    }

    public XContentBuilder startArray(String name, FieldCaseConversion conversion) throws IOException {
        field(name, conversion);
        startArray();
        return this;
    }

    public XContentBuilder startArray(String name) throws IOException {
        field(name);
        startArray();
        return this;
    }

    public XContentBuilder startArray() throws IOException {
        generator.writeStartArray();
        return this;
    }

    public XContentBuilder endArray() throws IOException {
        generator.writeEndArray();
        return this;
    }

    public XContentBuilder field(String name) throws IOException {
        if (fieldCaseConversion == FieldCaseConversion.UNDERSCORE) {
            if (cachedStringBuilder == null) {
                cachedStringBuilder = new StringBuilder();
            }
            name = toUnderscoreCase(name, cachedStringBuilder);
        } else if (fieldCaseConversion == FieldCaseConversion.CAMELCASE) {
            if (cachedStringBuilder == null) {
                cachedStringBuilder = new StringBuilder();
            }
            name = toCamelCase(name, cachedStringBuilder);
        }
        generator.writeFieldName(name);
        return this;
    }

    public XContentBuilder field(String name, FieldCaseConversion conversion) throws IOException {
        if (conversion == FieldCaseConversion.UNDERSCORE) {
            if (cachedStringBuilder == null) {
                cachedStringBuilder = new StringBuilder();
            }
            name = toUnderscoreCase(name, cachedStringBuilder);
        } else if (conversion == FieldCaseConversion.CAMELCASE) {
            if (cachedStringBuilder == null) {
                cachedStringBuilder = new StringBuilder();
            }
            name = toCamelCase(name, cachedStringBuilder);
        }
        generator.writeFieldName(name);
        return this;
    }

    public XContentBuilder field(String name, char[] value, int offset, int length) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeString(value, offset, length);
        }
        return this;
    }

    public XContentBuilder field(String name, String value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeString(value);
        }
        return this;
    }

    public XContentBuilder fieldIfNotNull(String name, String value) throws IOException {
        if (value != null) {
            field(name);
            generator.writeString(value);
        }
        return this;
    }

    public XContentBuilder field(String name, String value, FieldCaseConversion conversion) throws IOException {
        field(name, conversion);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeString(value);
        }
        return this;
    }

    public XContentBuilder fieldIfNotNull(String name, String value, FieldCaseConversion conversion) throws IOException {
        if (value != null) {
            field(name, conversion);
            generator.writeString(value);
        }
        return this;
    }

    public XContentBuilder field(String name, Integer value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeNumber(value.intValue());
        }
        return this;
    }

    public XContentBuilder fieldIfNotNull(String name, Integer value) throws IOException {
        if (value != null) {
            field(name);
            generator.writeNumber(value.intValue());
        }
        return this;
    }

    public XContentBuilder field(String name, int value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder field(String name, Long value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeNumber(value.longValue());
        }
        return this;
    }

    public XContentBuilder fieldIfNotNull(String name, Long value) throws IOException {
        if (value != null) {
            field(name);
            generator.writeNumber(value.longValue());
        }
        return this;
    }

    public XContentBuilder field(String name, long value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder field(String name, Float value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeNumber(value.floatValue());
        }
        return this;
    }

    public XContentBuilder fieldIfNotNull(String name, Float value) throws IOException {
        if (value != null) {
            field(name);
            generator.writeNumber(value.floatValue());
        }
        return this;
    }

    public XContentBuilder field(String name, float value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder field(String name, Double value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeNumber(value);
        }
        return this;
    }

    public XContentBuilder fieldIfNotNull(String name, Double value) throws IOException {
        if (value != null) {
            field(name);
            generator.writeNumber(value);
        }
        return this;
    }

    public XContentBuilder field(String name, double value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder field(String name, BigDecimal value) throws IOException {
        return field(name, value, value.scale(), RoundingMode.HALF_UP, true);
    }

    public XContentBuilder field(String name, BigDecimal value, int scale, RoundingMode rounding, boolean toDouble) throws IOException {
        field(name);
        if (toDouble) {
            try {
                generator.writeNumber(value.setScale(scale, rounding).doubleValue());
            } catch (ArithmeticException e) {
                generator.writeString(value.toEngineeringString());
            }
        } else {
            generator.writeString(value.toEngineeringString());
        }
        return this;
    }

    public XContentBuilder field(String name, BytesReference value) throws IOException {
        field(name);
        if (!value.hasArray()) {
            value = value.toBytesArray();
        }
        generator.writeBinary(value.array(), value.arrayOffset(), value.length());
        return this;
    }

    public XContentBuilder field(String name, byte[] value, int offset, int length) throws IOException {
        field(name);
        generator.writeBinary(value, offset, length);
        return this;
    }

    public XContentBuilder field(String name, Map<String, Object> value) throws IOException {
        field(name);
        value(value);
        return this;
    }

    public XContentBuilder field(String name, Iterable value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder field(String name, String... value) throws IOException {
        startArray(name);
        for (String o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder field(String name, Object... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder field(String name, int... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder field(String name, long... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder field(String name, float... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder field(String name, double... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder field(String name, Object value) throws IOException {
        if (value == null) {
            nullField(name);
            return this;
        }
        Class type = value.getClass();
        if (type == String.class) {
            field(name, (String) value);
        } else if (type == Float.class) {
            field(name, ((Float) value).floatValue());
        } else if (type == Double.class) {
            field(name, ((Double) value).doubleValue());
        } else if (type == Integer.class) {
            field(name, ((Integer) value).intValue());
        } else if (type == Long.class) {
            field(name, ((Long) value).longValue());
        } else if (type == Short.class) {
            field(name, ((Short) value).shortValue());
        } else if (type == Byte.class) {
            field(name, ((Byte) value).byteValue());
        } else if (type == Boolean.class) {
            field(name, ((Boolean) value).booleanValue());
        } else if (type == byte[].class) {
            field(name, (byte[]) value);
        } else if (value instanceof Map) {
            //noinspection unchecked
            field(name, (Map<String, Object>) value);
        } else if (value instanceof Iterable) {
            field(name, (Iterable) value);
        } else if (value instanceof Object[]) {
            field(name, (Object[]) value);
        } else if (value instanceof int[]) {
            field(name, (int[]) value);
        } else if (value instanceof long[]) {
            field(name, (long[]) value);
        } else if (value instanceof float[]) {
            field(name, (float[]) value);
        } else if (value instanceof double[]) {
            field(name, (double[]) value);
        } else if (value instanceof BytesReference) {
            field(name, (BytesReference) value);
        } else if (value instanceof ToXContent) {
            field(name, (ToXContent) value);
        } else {
            field(name, value.toString());
        }
        return this;
    }

    public XContentBuilder fieldIfNotNull(String name, Object value) throws IOException {
        if (value != null) {
            return field(name, value);
        }
        return this;
    }

    public XContentBuilder value(Object value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        Class type = value.getClass();
        if (type == String.class) {
            value((String) value);
        } else if (type == Float.class) {
            value(((Float) value).floatValue());
        } else if (type == Double.class) {
            value(((Double) value).doubleValue());
        } else if (type == Integer.class) {
            value(((Integer) value).intValue());
        } else if (type == Long.class) {
            value(((Long) value).longValue());
        } else if (type == Short.class) {
            value(((Short) value).shortValue());
        } else if (type == Byte.class) {
            value(((Byte) value).byteValue());
        } else if (type == Boolean.class) {
            value((Boolean) value);
        } else if (type == byte[].class) {
            value((byte[]) value);
        } else if (value instanceof BytesReference) {
            value((BytesReference) value);
        } else if (value instanceof Map) {
            value((Map<String, Object>) value);
        } else if (value instanceof Iterable) {
            value((Iterable) value);
        } else {
            throw new IOException("Type not allowed [" + type + "]");
        }
        return this;
    }

    public XContentBuilder field(String name, boolean value) throws IOException {
        field(name);
        generator.writeBoolean(value);
        return this;
    }

    public XContentBuilder field(String name, byte[] value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeBinary(value);
        }
        return this;
    }

    public XContentBuilder nullField(String name) throws IOException {
        generator.writeNullField(name);
        return this;
    }

    public XContentBuilder nullValue() throws IOException {
        generator.writeNull();
        return this;
    }

    public XContentBuilder rawField(String fieldName, byte[] content) throws IOException {
        generator.writeRawField(fieldName, content, bos);
        return this;
    }

    public XContentBuilder rawField(String fieldName, byte[] content, int offset, int length) throws IOException {
        generator.writeRawField(fieldName, content, offset, length, bos);
        return this;
    }

    public XContentBuilder rawField(String fieldName, InputStream content) throws IOException {
        generator.writeRawField(fieldName, content, bos);
        return this;
    }

    public XContentBuilder rawField(String fieldName, BytesReference content) throws IOException {
        generator.writeRawField(fieldName, content, bos);
        return this;
    }

    public XContentBuilder copy(XContentBuilder builder) throws IOException {
        generator.copy(builder, bos);
        return this;
    }

    public XContentBuilder copy(List<XContentBuilder> builder) throws IOException {
        for (int i = 0; i < builder.size(); i++) {
            if (i > 0) {
                bos.write(',');
            }
            generator.copy(builder.get(i), bos);
        }
        return this;
    }

    public XContentBuilder value(Boolean value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        return value(value.booleanValue());
    }

    public XContentBuilder value(boolean value) throws IOException {
        generator.writeBoolean(value);
        return this;
    }

    public XContentBuilder value(Integer value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        return value(value.intValue());
    }

    public XContentBuilder value(int value) throws IOException {
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder value(Long value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        return value(value.longValue());
    }

    public XContentBuilder value(long value) throws IOException {
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder value(Float value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        return value(value.floatValue());
    }

    public XContentBuilder value(float value) throws IOException {
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder value(Double value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        return value(value.doubleValue());
    }

    public XContentBuilder value(double value) throws IOException {
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder value(String value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        generator.writeString(value);
        return this;
    }

    public XContentBuilder value(byte[] value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        generator.writeBinary(value);
        return this;
    }

    public XContentBuilder value(byte[] value, int offset, int length) throws IOException {
        if (value == null) {
            return nullValue();
        }
        generator.writeBinary(value, offset, length);
        return this;
    }

    public XContentBuilder value(BytesReference value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        if (!value.hasArray()) {
            value = value.toBytesArray();
        }
        generator.writeBinary(value.array(), value.arrayOffset(), value.length());
        return this;
    }

    public XContentBuilder map(Map<String, Object> map) throws IOException {
        if (map == null) {
            return nullValue();
        }
        XContentMapConverter.writeMap(generator, map);
        return this;
    }

    public XContentBuilder value(Map<String, Object> map) throws IOException {
        if (map == null) {
            return nullValue();
        }
        XContentMapConverter.writeMap(generator, map);
        return this;
    }

    public XContentBuilder value(Iterable value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        startArray();
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder copyCurrentStructure(XContentParser parser) throws IOException {
        generator.copyCurrentStructure(parser);
        return this;
    }

    public XContentBuilder flush() throws IOException {
        generator.flush();
        return this;
    }

    public void close() {
        try {
            generator.close();
        } catch (IOException e) {
            // ignore
        }
    }

    public Object payload() {
        return this.payload;
    }

    public OutputStream stream() {
        return this.bos;
    }

    public BytesReference bytes() {
        close();
        return ((BytesStream) bos).bytes();
    }

    /**
     * Returns the actual stream used.
     */
    public BytesStream bytesStream() throws IOException {
        close();
        return (BytesStream) bos;
    }

    /**
     * Returns a string representation of the builder (only applicable for text based xcontent).
     * <p/>
     * <p>Only applicable when the builder is constructed with {@link FastByteArrayOutputStream}.
     */
    public String string() throws IOException {
        close();
        BytesArray bytesArray = bytes().toBytesArray();
        return new String(bytesArray.array(), bytesArray.arrayOffset(), bytesArray.length(), "UTF-8");
    }



    public static String toCamelCase(String value) {
        return toCamelCase(value, null);
    }

    public static String toCamelCase(String value, StringBuilder sb) {
        boolean changed = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '_') {
                if (!changed) {
                    if (sb != null) {
                        sb.setLength(0);
                    } else {
                        sb = new StringBuilder();
                    }
                    // copy it over here
                    for (int j = 0; j < i; j++) {
                        sb.append(value.charAt(j));
                    }
                    changed = true;
                }
                sb.append(Character.toUpperCase(value.charAt(++i)));
            } else {
                if (changed) {
                    sb.append(c);
                }
            }
        }
        if (!changed) {
            return value;
        }
        return sb.toString();
    }

    public static String toUnderscoreCase(String value) {
        return toUnderscoreCase(value, null);
    }

    public static String toUnderscoreCase(String value, StringBuilder sb) {
        boolean changed = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isUpperCase(c)) {
                if (!changed) {
                    if (sb != null) {
                        sb.setLength(0);
                    } else {
                        sb = new StringBuilder();
                    }
                    // copy it over here
                    for (int j = 0; j < i; j++) {
                        sb.append(value.charAt(j));
                    }
                    changed = true;
                    if (i == 0) {
                        sb.append(Character.toLowerCase(c));
                    } else {
                        sb.append('_');
                        sb.append(Character.toLowerCase(c));
                    }
                } else {
                    sb.append('_');
                    sb.append(Character.toLowerCase(c));
                }
            } else {
                if (changed) {
                    sb.append(c);
                }
            }
        }
        if (!changed) {
            return value;
        }
        return sb.toString();
    }
}
