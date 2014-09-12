/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU Affero General Public License as published 
 * by the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses 
 * or write to the Free Software Foundation, Inc., 51 Franklin Street, 
 * Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * The interactive user interfaces in modified source and object code 
 * versions of this program must display Appropriate Legal Notices, 
 * as required under Section 5 of the GNU Affero General Public License.
 * 
 * In accordance with Section 7(b) of the GNU Affero General Public 
 * License, these Appropriate Legal Notices must retain the display of the 
 * "Powered by xbib" logo. If the display of the logo is not reasonably 
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.marc;

import org.xbib.io.field.BufferedFieldStreamReader;
import org.xbib.marc.event.FieldEventListener;
import org.xbib.marc.transformer.StringTransformer;
import org.xbib.marc.xml.MarcXchangeSaxAdapter;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * ISO 2709 reader behaving like a SaX XMLReader
 */
public class Iso2709Reader implements XMLReader, MarcXchangeConstants {

    /**
     * The format property
     */
    public static String FORMAT = "format";
    /**
     * The type property
     */
    public static String TYPE = "type";

    /**
     * Should errors abort the reader.
     */
    public static String FATAL_ERRORS = "fatal_errors";

    /**
     * Should errors be silenced
     */
    public static String SILENT_ERRORS = "silent_errors";

    /**
     * Should the ISO 25577 tags be clean (validateable)?
     * All erraneous tags will be assigned to "999".
     * This mode is active by default.
     */
    public static String CLEAN_TAGS = "clean_tags";

    /**
     * Shall all data be XML 1.0 safe?
     */
    public static String SCRUB_DATA = "scrub_data";

    /**
     * Buffer size for input stream
     */
    public static String BUFFER_SIZE = "buffer_size";

    /**
     * The schema property
     */
    public static String SCHEMA = "schema";

    /**
     * Subfield delimiter
     */
    public static String SUBFIELD_DELIMITER = "subfield_delimiter";

    /**
     * Subfield delimiter
     */
    public static String SUBFIELD_CODE_LENGTH = "subfield_code_length";

    public static String FIELDMAPPER = "field_mapper";

    /**
     * The SaX service
     */
    private MarcXchangeSaxAdapter adapter;
    /**
     * XML content handler
     */
    private ContentHandler contentHandler;

    private EntityResolver entityResolver;

    private DTDHandler dtdHandler;

    private ErrorHandler errorHandler;

    private Map<String, Boolean> features = new HashMap<String, Boolean>();

    /**
     * Properties for this reader
     */
    private Map<String, Object> properties = new HashMap<String, Object>() {
        {
            put(FORMAT, MARC21);
            put(TYPE, BIBLIOGRAPHIC);
            put(FATAL_ERRORS, Boolean.FALSE);
            put(SILENT_ERRORS, Boolean.FALSE);
            put(BUFFER_SIZE, 65536);
        }
    };

    public Iso2709Reader() {
        this.adapter = new MarcXchangeSaxAdapter();
    }

    /**
     * Get the MarcXchange Sax service. Useful for inserting MarcXchange data
     * to the MarcXchange listener.
     * @return the MarcXchange Sax service
     */
    public MarcXchangeSaxAdapter getAdapter() {
        return adapter;
    }

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
         return features.get(name);
    }

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        this.features.put(name, value);
    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return properties.get(name);
    }

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        properties.put(name, value);
    }

    @Override
    public void setEntityResolver(EntityResolver resolver) {
       this.entityResolver = resolver;
    }

    @Override
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    @Override
    public void setDTDHandler(DTDHandler handler) {
        this.dtdHandler = handler;
    }

    @Override
    public DTDHandler getDTDHandler() {
        return dtdHandler;
    }

    @Override
    public void setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
    }

    @Override
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    @Override
    public void setErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Set MarcXchange listener for this reader.
     * @param listener the MarcXchange listener
     * @return this reader
     */
    public Iso2709Reader setMarcXchangeListener(MarcXchangeListener listener) {
        this.adapter.setMarcXchangeListener(listener);
        return this;
    }

    public Iso2709Reader setMarcXchangeListener(String type, MarcXchangeListener listener) {
        this.adapter.setMarcXchangeListener(type, listener);
        return this;
    }

    public Iso2709Reader setTransformer(StringTransformer transformer) {
        this.adapter.setTransformer(transformer);
        return this;
    }

    public Iso2709Reader addFieldMap(String fieldMapName, Map<String, Object> fieldMap) {
        this.adapter.addFieldMap(fieldMapName, fieldMap);
        properties.put(FIELDMAPPER, Boolean.TRUE);
        return this;
    }

    public boolean isFieldMapped() {
        return properties.get(FIELDMAPPER) != null;
    }

    public Iso2709Reader setFieldEventListener(FieldEventListener fieldEventListener) {
        this.adapter.setFieldEventListener(fieldEventListener);
        return this;
    }

    public Iso2709Reader setFormat(String format) {
        properties.put(FORMAT, format);
        return this;
    }

    public String getFormat() {
        return (String) properties.get(FORMAT);
    }

    public Iso2709Reader setType(String type) {
        properties.put(TYPE, type);
        return this;
    }

    public String getType() {
        return (String) properties.get(TYPE);
    }

    public Iso2709Reader setSubfieldDelimiter(String delimiter) {
        properties.put(SUBFIELD_DELIMITER, delimiter);
        return this;
    }

    public Iso2709Reader setSubfieldDelimiter(char delimiter) {
        properties.put(SUBFIELD_DELIMITER, Character.toString(delimiter));
        return this;
    }

    public Iso2709Reader setSubfieldCodeLength(Integer subfieldCodeLength) {
        properties.put(SUBFIELD_CODE_LENGTH, subfieldCodeLength);
        return this;
    }

    public Iso2709Reader setCleanTags(Boolean cleanTags) {
        properties.put(CLEAN_TAGS, cleanTags);
        return this;
    }

    public Iso2709Reader setScrubData(Boolean scrubData) {
        properties.put(SCRUB_DATA, scrubData);
        return this;
    }

    private MarcXchangeSaxAdapter setup(MarcXchangeSaxAdapter adapter) {
        Boolean fatalErrors = properties.get(FATAL_ERRORS) != null ?
                (properties.get(FATAL_ERRORS) instanceof Boolean ? (Boolean)properties.get(FATAL_ERRORS) :
                Boolean.parseBoolean((String)properties.get(FATAL_ERRORS))) : null;
        Boolean silentErrors = properties.get(SILENT_ERRORS) != null ?
                (properties.get(SILENT_ERRORS) instanceof Boolean ? (Boolean)properties.get(SILENT_ERRORS) :
                Boolean.parseBoolean((String)properties.get(SILENT_ERRORS))) : null;
        Boolean cleanTags = properties.get(CLEAN_TAGS) != null ?
                (properties.get(CLEAN_TAGS) instanceof Boolean ? (Boolean)properties.get(CLEAN_TAGS) :
                        Boolean.parseBoolean((String)properties.get(CLEAN_TAGS))) : Boolean.TRUE;
        Boolean scrubData = properties.get(SCRUB_DATA) != null ?
                (properties.get(SCRUB_DATA) instanceof Boolean ? (Boolean)properties.get(SCRUB_DATA) :
                        Boolean.parseBoolean((String)properties.get(SCRUB_DATA))) : Boolean.TRUE;
        Integer subfieldCodeLength = properties.get(SUBFIELD_CODE_LENGTH) != null ?
                (properties.get(SUBFIELD_CODE_LENGTH) instanceof  Integer ? (Integer) properties.get(SUBFIELD_CODE_LENGTH) :
                Integer.parseInt((String)properties.get(SUBFIELD_CODE_LENGTH))) : null;
        return adapter.setBuffersize((Integer)properties.get(BUFFER_SIZE))
                .setContentHandler(contentHandler)
                .setSchema((String) properties.get(SCHEMA))
                .setFormat(getFormat())
                .setType(getType())
                .setFatalErrors(fatalErrors)
                .setSilentErrors(silentErrors)
                .setSubfieldDelimiter((String)properties.get(SUBFIELD_DELIMITER))
                .setSubfieldIdLength(subfieldCodeLength)
                .setCleanTags(cleanTags)
                .setScrubData(scrubData);
    }

    public BufferedFieldStreamReader stream(InputStream in) throws IOException {
        return setup(adapter).setInputStream(in).fieldStream();
    }

    public BufferedFieldStreamReader mappedStream(InputStream in) throws IOException {
        return setup(adapter).setInputStream(in).mappedFieldStream();
    }

    public BufferedFieldStreamReader stream(Reader reader) throws IOException {
        return setup(adapter).setReader(reader).fieldStream();
    }

    public BufferedFieldStreamReader mappedStream(Reader reader) throws IOException {
        return setup(adapter).setReader(reader).mappedFieldStream();
    }

    public void parse(InputStream in) throws IOException {
        parse(new InputStreamReader(in, "UTF-8"));
    }

    public void parse(Reader reader) throws IOException {
        try {
            parse(new InputSource(reader));
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void parse(InputSource input) throws IOException, SAXException {
        setup(adapter).setInputSource(input)
                .parseCollection(isFieldMapped() ? adapter.mappedFieldStream() : adapter.fieldStream());
    }

    /**
     * We do not support system ID based parsing.
     * @param systemId the system ID
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void parse(String systemId) throws IOException, SAXException {
        throw new UnsupportedOperationException();
    }
}
