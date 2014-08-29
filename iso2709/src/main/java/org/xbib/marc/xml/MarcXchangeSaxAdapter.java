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
package org.xbib.marc.xml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.regex.Pattern;

import org.xbib.io.field.BufferedFieldStreamReader;
import org.xbib.io.field.FieldListener;
import org.xbib.io.field.FieldSeparator;
import org.xbib.io.field.FieldStream;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldDirectory;
import org.xbib.marc.InvalidFieldDirectoryException;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.RecordLabel;
import org.xbib.marc.normalize.ValueNormalizer;
import org.xbib.marc.normalize.WithoutNormalizer;
import org.xbib.marc.xml.mapper.MarcXchangeFieldMapper;
import org.xbib.xml.XMLNS;
import org.xbib.xml.XSI;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * The MarcXchange Sax adapter converts ISO 2709 stream events to MarcXchange events.
 * It can also be used to map fields.
 */
public class MarcXchangeSaxAdapter extends MarcXchangeFieldMapper
        implements MarcXchangeConstants, MarcXchangeListener {

    private static final Logger logger = LoggerFactory.getLogger(MarcXchangeSaxAdapter.class.getName());

    private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

    private ValueNormalizer normalizer = new WithoutNormalizer();

    private Reader reader;

    private char mark = '\u0000';

    private int position = 0;

    private FieldDirectory directory;

    private Field designator;

    private RecordLabel recordLabel;

    private boolean datafieldOpen;

    private boolean subfieldOpen;

    private boolean recordOpen;

    private String schema;

    private String id;

    private String nsUri;

    private ContentHandler contentHandler;

    private MarcXchangeListener listener;

    private boolean fatalerrors = false;

    private boolean silenterrors = false;

    private int buffersize = 65536;

    private String subfieldDelimiter = null;

    public MarcXchangeSaxAdapter() {
        this.nsUri = NS_URI;
        this.subfieldOpen = false;
        this.recordOpen = false;
        this.subfieldDelimiter = null;
    }

    public MarcXchangeSaxAdapter setBuffersize(int buffersize) {
        this.buffersize = buffersize;
        return this;
    }

    public MarcXchangeSaxAdapter setInputSource(final InputSource source) throws IOException {
        if (source.getByteStream() != null) {
            String encoding = source.getEncoding() != null ? source.getEncoding() : "ANSEL";
            this.reader = new InputStreamReader(source.getByteStream(), encoding);
        } else {
            this.reader = source.getCharacterStream();
        }
        return this;
    }

    public MarcXchangeSaxAdapter setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
        return this;
    }

    public MarcXchangeSaxAdapter setListener(MarcXchangeListener listener) {
        this.listener = listener;
        return this;
    }

    public MarcXchangeSaxAdapter setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public MarcXchangeSaxAdapter setFormat(String format) {
        super.setFormat(format);
        return this;
    }

    public MarcXchangeSaxAdapter setType(String type) {
        super.setType(type);
        return this;
    }
    
    public MarcXchangeSaxAdapter setFatalErrors(Boolean fatalerrors) {
        this.fatalerrors = fatalerrors;
        return this;
    }

    public MarcXchangeSaxAdapter setSilentErrors(Boolean silenterrors) {
        this.silenterrors = silenterrors;
        return this;
    }

    public MarcXchangeSaxAdapter setValueNormalizer(ValueNormalizer normalizer) {
        if (normalizer != null) {
            this.normalizer = normalizer;
        }
        return this;
    }

    public MarcXchangeSaxAdapter setSubfieldDelimiter(String subfieldDelimiter) {
        this.subfieldDelimiter = subfieldDelimiter;
        return this;
    }

    public String getIdentifier() {
        return id;
    }

    public MarcXchangeSaxAdapter setFieldMap(Map<String, Object> map) {
        super.setFieldMap(map);
        return this;
    }

    /**
     * Parse ISO 2709 and emit SAX events.
     */
    public void parse() throws IOException, SAXException {
        FieldListener fieldListener = getFieldMap() != null ? new MappedStreamListener() : new DirectListener();
        FieldStream stream = new BufferedFieldStreamReader(reader, buffersize, fieldListener);
        beginCollection();
        String chunk;
        do {
            chunk = stream.readData();
        } while (chunk != null);
        stream.close();
        endCollection();
    }

    @Override
    public void beginCollection() {
        if (contentHandler == null) {
            return;
        }
        try {
            contentHandler.startDocument();
            // write schema info
            AttributesImpl attrs = new AttributesImpl();
            if ("MARC21".equalsIgnoreCase(schema)) {
                this.nsUri = MARC21_NS_URI;
                attrs.addAttribute(XMLNS.NS_URI, XSI.NS_PREFIX,
                        XMLNS.NS_PREFIX + ":" + XSI.NS_PREFIX, "CDATA", XSI.NS_URI);
                attrs.addAttribute(XSI.NS_URI, "schemaLocation",
                        XSI.NS_PREFIX + ":schemaLocation", "CDATA", MARC21_NS_URI + " " + MARC21_SCHEMALOCATION);

            } else {
                this.nsUri = NS_URI;
                attrs.addAttribute(XMLNS.NS_URI, XSI.NS_PREFIX,
                        XMLNS.NS_PREFIX + ":" + XSI.NS_PREFIX, "CDATA", XSI.NS_URI);
                attrs.addAttribute(XSI.NS_URI, "schemaLocation",
                        XSI.NS_PREFIX + ":schemaLocation", "CDATA", NS_URI + " " + MARCXCHANGE_SCHEMALOCATION);
            }
            contentHandler.startPrefixMapping("", nsUri);
            contentHandler.startElement(nsUri, COLLECTION, COLLECTION, attrs);
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void endCollection() {
        if (contentHandler == null) {
            return;
        }
        try {
            contentHandler.endElement(nsUri, COLLECTION, COLLECTION);
            contentHandler.endDocument();
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void beginRecord(String format, String type) {
        if (recordOpen) {
            return;
        }
        try {
            AttributesImpl attrs = new AttributesImpl();
            if (format != null && !"MARC21".equalsIgnoreCase(schema)) {
                attrs.addAttribute(nsUri, FORMAT, FORMAT, "CDATA", format);
            }
            if (type != null) {
                attrs.addAttribute(nsUri, TYPE, TYPE, "CDATA", type);
            }
            if (contentHandler != null) {
                contentHandler.startElement(nsUri, RECORD, RECORD, attrs);
            }
            if (listener != null) {
                listener.beginRecord(format, type);
            }
            this.recordOpen = true;
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void endRecord() {
        if (!recordOpen) {
            return;
        }
        try {
            if (contentHandler != null) {
                contentHandler.endElement(nsUri, RECORD, RECORD);
            }
            if (listener != null) {
                listener.endRecord();
            }
            this.recordOpen = false;
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void leader(String value) {
        if (value == null) {
            return;
        }
        try {
            if (contentHandler != null) {
                contentHandler.startElement(nsUri, LEADER, LEADER, EMPTY_ATTRIBUTES);
                contentHandler.characters(value.toCharArray(), 0, value.length());
                contentHandler.endElement(nsUri, LEADER, LEADER);
            }
            if (listener != null) {
                listener.leader(value);
            }
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void beginControlField(Field designator) {
        if (designator == null) {
            return;
        }
        try {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute(nsUri, TAG, TAG, "CDATA", designator.tag());
            if (contentHandler != null) {
                contentHandler.startElement(nsUri, CONTROLFIELD, CONTROLFIELD, attrs);
            }
            if (listener != null) {
                listener.beginControlField(designator);
            }
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void endControlField(Field designator) {
        try {
            if (listener != null) {
                listener.endControlField(designator);
            }
            if (designator != null) {
                String value = designator.data();
                if (!value.isEmpty()) {
                    switch (designator.tag()) {
                        case "001":
                            this.id = value;
                            break;
                        case "006":
                        case "007":
                        case "008":
                            // fix fill characters here
                            value = value.replace('^', '|');
                            break;
                    }
                    if (contentHandler != null) {
                        contentHandler.characters(value.toCharArray(), 0, value.length());
                    }
                }
            }
            if (contentHandler != null) {
                contentHandler.endElement(nsUri, CONTROLFIELD, CONTROLFIELD);
            }
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void beginDataField(Field designator) {
        if (designator == null) {
            return;
        }
        try {
            if (designator.isControlField()) {
                beginControlField(designator);
                endControlField(designator);
                return;
            }
            if (datafieldOpen) {
                return;
            }
            AttributesImpl attrs = new AttributesImpl();
            String tag = designator.tag();
            if (tag == null || tag.length() == 0) {
                tag = Field.NULL_TAG; // fallback
                designator.tag(tag);
            }
            attrs.addAttribute(nsUri, TAG, TAG, "CDATA", tag);
            int ind = designator.indicator() != null
                    ? designator.indicator().length() : 0;
            // force at least two default blank indicators if schema is Marc 21
            if ("MARC21".equalsIgnoreCase(schema)) {
                for (int i = (ind == 0 ? 1 : ind); i <= 2; i++) {
                    attrs.addAttribute(null, IND + i, IND + i, "CDATA", " ");
                }
            }
            // set indicators
            for (int i = 1; i <= ind; i++) {
                attrs.addAttribute(null, IND + i,
                        IND + i, "CDATA", designator.indicator().substring(i - 1, i));
            }
            if (contentHandler != null) {
                contentHandler.startElement(nsUri, DATAFIELD, DATAFIELD, attrs);
            }
            if (listener != null) {
                listener.beginDataField(designator);
            }
            datafieldOpen = true;
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void endDataField(Field designator) {
        try {
            if (!datafieldOpen) {
                return;
            }
            if (designator != null) {
                String value = designator.data();
                if (value != null && !value.isEmpty() && subfieldDelimiter == null) {
                    if (normalizer != null) {
                        value = normalizer.normalize(value);
                    }
                    // write data field per default into a subfield with code 'a'
                    AttributesImpl attrs = new AttributesImpl();
                    attrs.addAttribute(nsUri, CODE, CODE, "CDATA", "a");
                    if (contentHandler != null) {
                        contentHandler.startElement(nsUri, SUBFIELD, SUBFIELD, attrs);
                        contentHandler.characters(value.toCharArray(), 0, value.length());
                        contentHandler.endElement(nsUri, SUBFIELD, SUBFIELD);
                    }
                }
            }
            if (listener != null) {
                listener.endDataField(designator);
            }
            if (contentHandler != null) {
                contentHandler.endElement(NS_URI, DATAFIELD, DATAFIELD);
            }
            datafieldOpen = false;
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void beginSubField(Field designator) {
        if (designator == null) {
            return;
        }
        try {
            AttributesImpl attrs = new AttributesImpl();
            String subfieldId = designator.subfieldId();
            if (subfieldId == null || subfieldId.length() == 0) {
                subfieldId = "a"; // fallback
            }
            attrs.addAttribute(nsUri, CODE, CODE, "CDATA", subfieldId);
            if (listener != null) {
                listener.beginSubField(designator);
            }
            if (contentHandler != null) {
                contentHandler.startElement(nsUri, SUBFIELD, SUBFIELD, attrs);
            }
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void endSubField(Field designator) {
        if (designator == null) {
            return;
        }
        try {
            if (contentHandler != null) {
                String value = designator.data();
                if (value != null && !value.isEmpty()) {
                    if (normalizer != null) {
                        value = normalizer.normalize(value);
                    }
                    contentHandler.characters(value.toCharArray(), 0, value.length());
                }
            }
            if (listener != null) {
                listener.endSubField(designator);
            }
            if (contentHandler != null) {
                contentHandler.endElement(NS_URI, SUBFIELD, SUBFIELD);
            }
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    private class DirectListener implements FieldListener {

        @SuppressWarnings("fallthrough")
        @Override
        public void data(String data) {
            String fieldContent = data;
            try {
                switch (mark) {
                    case FieldSeparator.FS: // start/end file
                        break;
                    case FieldSeparator.GS: {
                        // start/end of group within a stream
                        if (subfieldOpen) { // close subfield if open
                            subfieldOpen = false;
                            endDataField(null);
                        }
                        if (recordLabel.getIndicatorLength() > 1 || subfieldDelimiter != null) {
                            endDataField(designator.data(""));
                        } else {
                            endDataField(designator);
                        }
                        endRecord(); // close record
                        // fall through is ok!
                    }
                    case '\u0000': {
                        // start of stream
                        position = 0;
                        // skip line-feed (OCLC PICA quirk)
                        if (data.charAt(0) == '\n') {
                            fieldContent = data.substring(1);
                        }
                        if (fieldContent.length() >= RecordLabel.LENGTH) {
                            beginRecord(getFormat(), getType());
                            String labelStr = fieldContent.substring(0, RecordLabel.LENGTH);
                            recordLabel = new RecordLabel(labelStr.toCharArray());
                            // auto-repair label
                            leader(recordLabel.getRecordLabel());
                            directory = new FieldDirectory(recordLabel, fieldContent);
                            if (directory.isEmpty()) {
                                designator = new Field(recordLabel, fieldContent.substring(RecordLabel.LENGTH));
                                if (designator.tag() != null) {
                                    if (subfieldDelimiter != null) {
                                        // skip tag if custom subfield delimiter
                                        designator.data(fieldContent.substring(RecordLabel.LENGTH + 3));
                                    }
                                    beginDataField(designator);
                                }
                            }
                        } else {
                            directory = new FieldDirectory(recordLabel, fieldContent);
                            designator = new Field();
                        }
                        break;
                    }
                    case FieldSeparator.RS: {
                        if (subfieldOpen) {
                            subfieldOpen = false;
                            endDataField(null); // force data field close
                        } else if (designator != null && !designator.isEmpty()) {
                            if (datafieldOpen) {
                                if (recordLabel.getIndicatorLength() > 1 || subfieldDelimiter != null) {
                                    endDataField(designator.data(""));
                                } else {
                                    endDataField(designator);
                                }
                            }
                        }
                        if (directory == null || directory.isEmpty()) {
                            designator = new Field(recordLabel, fieldContent);
                        } else if (directory.containsKey(position)) {
                            designator = new Field(recordLabel, directory.get(position), fieldContent, false);
                        } else {
                            throw new InvalidFieldDirectoryException("byte position not found in directory: "
                                    + position + " - is this stream reading using an 8-bit wide encoding?");
                        }
                        // custom subfield delimiter? Can be useful if source does not split subfields
                        // with FieldSeparator.US but with pseudo delimiters like "$$"
                        if (subfieldDelimiter != null) {
                            if (!designator.isControlField()) {
                                beginDataField(designator);
                                // tricky: first field has no subfield ID. We set it to blank.
                                fieldContent = " " + fieldContent.substring(4);
                                for (String subfield : fieldContent.split(Pattern.quote(subfieldDelimiter))) {
                                    if (normalizer != null) {
                                        subfield = normalizer.normalize(subfield);
                                    }
                                    designator = new Field(recordLabel, designator, subfield, true);
                                    beginSubField(designator);
                                    endSubField(designator);
                                }
                            }
                        } else {
                            beginDataField(designator);
                        }
                        break;
                    }
                    case FieldSeparator.US: {
                        if (!subfieldOpen) {
                            subfieldOpen = true;
                            beginDataField(designator);
                        }
                        if (designator != null) {
                            if (normalizer != null) {
                                fieldContent = normalizer.normalize(fieldContent);
                            }
                            designator = new Field(recordLabel, designator, fieldContent, true);
                            beginSubField(designator);
                            endSubField(designator);
                        }
                        break;
                    }
                }
            } catch (InvalidFieldDirectoryException ex) {
                // we reciver from invalid field directories
                logger.warn(ex.getMessage());
            } finally {
                position += data.length();
            }
        }

        @Override
        public void mark(char separator) {
            mark = separator;
            position++;
            if (mark == FieldSeparator.FS) {
                if (datafieldOpen) {
                    endDataField(null); // close last data field if not closed already
                }
                endRecord();
            }
        }

    }

    /**
     * This field listener can map fields according to a given map and organize them into records.
     * Incoming fields (the subfield list) are processed one by one. At the end of record, the fields are
     * flushed out to the listener with the mapped version of data fields and subfields.
     * Data field opening and closing is controlled by the mapped fields.
     * A repeat counter <code>{r}</code> counts if source fields tag do repeat. The repeat counter
     * can be interpolated into the mapped field designator.
     */
    private class MappedStreamListener implements FieldListener {

        @SuppressWarnings("fallthrough")
        @Override
        public void data(String data) {
            String fieldContent = data;
            try {
                switch (mark) {
                    case FieldSeparator.FS: // start/end file
                        break;
                    case FieldSeparator.GS: {
                        // start/end of group within a stream
                        if (subfieldOpen) { // close subfield if open
                            subfieldOpen = false;
                            addDataField(Field.EMPTY); // force data field close event
                        }
                        if (recordLabel.getIndicatorLength() > 1 || subfieldDelimiter != null) {
                            addDataField(designator.data(""));
                        } else {
                            addDataField(designator);
                        }
                        flushField();
                        flushRecord();
                        // fall through is ok!
                    }
                    case '\u0000': {
                        // start of stream
                        position = 0;
                        // skip line-feed (OCLC PICA quirk)
                        if (data.charAt(0) == '\n') {
                            fieldContent = data.substring(1);
                        }
                        if (fieldContent.length() >= RecordLabel.LENGTH) {
                            String labelStr = fieldContent.substring(0, RecordLabel.LENGTH);
                            recordLabel = new RecordLabel(labelStr.toCharArray());
                            setRecordLabel(recordLabel.getRecordLabel());
                            // auto-repair label
                            directory = new FieldDirectory(recordLabel, fieldContent);
                            if (directory.isEmpty()) {
                                designator = new Field(recordLabel, fieldContent.substring(RecordLabel.LENGTH));
                                if (designator.tag() != null) {
                                    if (subfieldDelimiter != null) {
                                        // skip tag if custom subfield delimiter
                                        designator.data(fieldContent.substring(RecordLabel.LENGTH + 3));
                                    }
                                    if (designator.isControlField()) {
                                        addControlField(designator);
                                    } else {
                                        addDataField(designator);
                                    }
                                }
                            }
                        } else {
                            directory = new FieldDirectory(recordLabel, fieldContent);
                            designator = new Field();
                        }
                        break;
                    }
                    case FieldSeparator.RS: {
                        if (subfieldOpen) {
                            subfieldOpen = false;
                            addDataField(Field.EMPTY);
                            flushField();
                        } else if (designator != null && !designator.isEmpty()) {
                            if (datafieldOpen) {
                                addDataField(designator);
                                flushField();
                            }
                        }
                        if (directory == null || directory.isEmpty()) {
                            designator = new Field(recordLabel, fieldContent);
                        } else if (directory.containsKey(position)) {
                            designator = new Field(recordLabel, directory.get(position), fieldContent, false);
                        } else {
                            throw new InvalidFieldDirectoryException("byte position not found in directory: "
                                    + position + " - is this stream reading using an 8-bit wide encoding?");
                        }
                        // Is custom subfield delimiter set? Can be useful if source does not split subfields
                        // with FieldSeparator.US but with pseudo delimiters like "$$"
                        if (subfieldDelimiter != null) {
                            if (!designator.isControlField()) {
                                addDataField(designator);
                                // tricky: first field has no subfield ID. We set it to blank.
                                fieldContent = " " + fieldContent.substring(4);
                                for (String subfield : fieldContent.split(Pattern.quote(subfieldDelimiter))) {
                                    if (normalizer != null) {
                                        subfield = normalizer.normalize(subfield);
                                    }
                                    designator = new Field(recordLabel, designator, subfield, true);
                                    addDataField(designator);
                                }
                            }
                            flushField();
                        } else {
                            if (designator.isControlField()) {
                                addControlField(designator);
                            } else {
                                addDataField(designator);
                            }
                        }
                        break;
                    }
                    case FieldSeparator.US: {
                        if (!subfieldOpen) {
                            subfieldOpen = true;
                            addDataField(designator);
                        }
                        if (designator != null) {
                            if (normalizer != null) {
                                fieldContent = normalizer.normalize(fieldContent);
                            }
                            designator = new Field(recordLabel, designator, fieldContent, true);
                            addDataField(designator);
                        }
                        break;
                    }
                }
            } catch (InvalidFieldDirectoryException ex) {
                // we recover from invalid field directories here
                logger.warn(ex.getMessage());
            } finally {
                position += data.length();
            }
        }

        @Override
        public void mark(char separator) {
            mark = separator;
            position++;
            if (mark == FieldSeparator.FS) {
                flushRecord();
            }
        }
    }
}
