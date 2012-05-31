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
package org.xbib.marc.addons;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.xbib.importer.AbstractImporter;
import org.xbib.io.Connection;
import org.xbib.io.ConnectionManager;
import org.xbib.io.Mode;
import org.xbib.io.sql.Query;
import org.xbib.io.sql.SQLResultWithDelayedCloseProcessor;
import org.xbib.io.sql.SQLSession;
import org.xbib.marc.FieldDesignator;
import org.xbib.marc.MarcXchangeListener;

public class AlephPublishingReader extends AbstractImporter<Integer, Integer>
        implements MarcXchangeListener, Iterator<Integer> {

    private final static Logger logger = Logger.getLogger(AlephPublishingReader.class.getName());
    private XMLInputFactory factory = XMLInputFactory.newInstance();
    private final DecimalFormat df = new DecimalFormat("000000000");
    private final static int CLOB_BUF_SIZE = 8192;
    private URI uri;
    private Iterator<Integer> iterator;
    private Integer pos;
    private String library;
    private String name;
    private Connection<SQLSession> connection;
    private SQLSession session;
    private String clob;
    private StringBuilder sb = new StringBuilder();
    private MarcXchangeListener listener;
    private boolean prepared = false;
    private boolean inRecord = false;
    private final Object lock = new Object();

    public AlephPublishingReader() {
    }

    @Override
    public AlephPublishingReader setURI(URI uri) {
        this.uri = uri;
        return this;
    }

    public AlephPublishingReader setIterator(Iterator<Integer> iterator) {
        this.iterator = iterator;
        return this;
    }

    public AlephPublishingReader setLibrary(String library) {
        this.library = library;
        return this;
    }

    public AlephPublishingReader setSetName(String name) {
        this.name = name;
        return this;
    }

    public AlephPublishingReader setListener(MarcXchangeListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public boolean hasNext() {
        try {
            return prepareRead();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public Integer next() {
        return nextRead();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void beginRecord(String format, String type) {
        if (listener != null) {
            listener.beginRecord(format, type);
        }
    }

    @Override
    public void endRecord() {
        if (listener != null) {
            listener.endRecord();
        }
    }

    @Override
    public void leader(String label) {
        if (listener != null) {
            listener.leader(label);
        }
    }

    @Override
    public void trailer(String trailer) {
        if (listener != null) {
            listener.trailer(trailer);
        }
    }

    @Override
    public void beginControlField(FieldDesignator designator) {
        if (listener != null) {
            listener.beginControlField(designator);
        }
    }

    @Override
    public void endControlField(FieldDesignator designator) {
        if (listener != null) {
            listener.endControlField(designator);
        }
    }

    @Override
    public void beginDataField(FieldDesignator designator) {
        if (listener != null) {
            listener.beginDataField(designator);
        }
    }

    @Override
    public void endDataField(FieldDesignator designator) {
        if (listener != null) {
            listener.endDataField(designator);
        }
    }

    @Override
    public void beginSubField(FieldDesignator designator) {
        if (listener != null) {
            listener.beginSubField(designator);
        }
    }

    @Override
    public void endSubField(FieldDesignator designator) {
        if (listener != null) {
            listener.endSubField(designator);
        }
    }

    @Override
    public void close() throws IOException {
        if (session != null) {
            session.close();
            logger.log(Level.INFO, "session closed");
        }
        if (connection != null) {
            connection.close();
            logger.log(Level.INFO, "connection closed");
        }
    }

    public void release() throws IOException {
        if (iterator instanceof Closeable) {
            ((Closeable) iterator).close();
        }
    }

    private String getClob(Reader reader) throws IOException {
        if (reader == null) {
            return null;
        }
        char[] buffer = new char[CLOB_BUF_SIZE];
        StringBuilder out = new StringBuilder();
        int read;
        do {
            read = reader.read(buffer, 0, buffer.length);
            if (read > 0) {
                out.append(buffer, 0, read);
            }
        } while (read >= 0);
        return out.toString();
    }

    private boolean prepareRead() throws IOException {
        if (iterator == null) {
            return false;
        }
        if (prepared) {
            return true;
        }
        if (name == null) {
            throw new IllegalArgumentException("no set name given");
        }
        if (library == null) {
            throw new IllegalArgumentException("no library given");
        }
        boolean skip;
        do {
            skip = false;
            synchronized (lock) {
                this.pos = iterator.hasNext() ? iterator.next() : null;
            }
            if (pos == null) {
                this.prepared = false;
                return false;
            }
            if (session == null) {
                createSession();
            }
            final Map<String, String> params = new HashMap<>();
            params.put("docNumber", df.format(pos));
            final Query query = new Query("select z00p_str, z00p_ptr from "
                    + library + ".z00p where z00p_set = '" + name + "' and z00p_doc_number = ?",
                    new String[]{"docNumber"}, params);
            SQLResultWithDelayedCloseProcessor p = new SQLResultWithDelayedCloseProcessor();
            try {
                query.addProcessor(p);
                query.execute(session);
                ResultSet results = p.getResultSet();
                if ((results != null) && results.next()) {
                    try (Reader reader = results.getBytes(1) != null ? new InputStreamReader(new ByteArrayInputStream(results.getBytes(1)), "UTF-8") : results.getCharacterStream(2)) {
                        this.clob = getClob(reader);
                    }
                    this.prepared = true;
                    return true;
                } else {
                    logger.log(Level.WARNING, "skipped {0}", pos);
                    skip = true;
                }
            } catch (SQLException | IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            } finally {
                query.close();
                p.close();
            }
        } while (skip);
        this.prepared = false;
        return false;
    }

    private Integer nextRead() {
        if (clob == null) {
            return null;
        }
        try {
            try (StringReader sr = new StringReader(clob)) {
                XMLEventReader xmlReader = factory.createXMLEventReader(sr);
                Stack<FieldDesignator> stack = new Stack();
                while (xmlReader.hasNext()) {
                    processEvent(stack, xmlReader.peek());
                    xmlReader.nextEvent();
                }
            }
            trailer(clob);
        } catch (XMLStreamException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        // invalidate read
        prepared = false;
        return pos;
    }

    private void processEvent(Stack<FieldDesignator> stack, XMLEvent event) {
        if (event.isStartElement()) {
            StartElement element = (StartElement) event;
            String localName = element.getName().getLocalPart();
            Iterator<?> it = element.getAttributes();
            String format = null;
            String type = null;
            String tag = null;
            char ind1 = '\u0000';
            char ind2 = '\u0000';
            char code = '\u0000';
            while (it.hasNext()) {
                Attribute attr = (Attribute) it.next();
                QName attributeName = attr.getName();
                String attributeLocalName = attributeName.getLocalPart();
                String attributeValue = attr.getValue();
                switch (attributeLocalName) {
                    case "tag":
                        tag = attributeValue;
                        break;
                    case "ind1":
                        ind1 = attributeValue.charAt(0);
                        if (ind1 == '-') {
                            ind1 = ' '; // replace illegal blank symbols
                        }
                        break;
                    case "ind2":
                        ind2 = attributeValue.charAt(0);
                        if (ind2 == '-') {
                            ind2 = ' '; // replace illegal blank symbols
                        }
                        break;
                    case "code":
                        code = attributeValue.charAt(0);
                        break;
                    case "format":
                        format = attributeValue;
                        break;
                    case "type":
                        type = attributeValue;
                        break;
                }
            }
            switch (localName) {
                case "subfield": {
                    FieldDesignator f = stack.peek();
                    FieldDesignator subfield = new FieldDesignator(f.getTag(), f.getIndicator(), Character.toString(code));
                    stack.push(subfield);
                    beginSubField(subfield);
                    break;
                }
                case "datafield": {
                    FieldDesignator field = ind2 != '\u0000'
                            ? new FieldDesignator(tag, Character.toString(ind1) + Character.toString(ind2))
                            : new FieldDesignator(tag, Character.toString(ind1));
                    stack.push(field);
                    beginDataField(field);
                    break;
                }
                case "controlfield": {
                    FieldDesignator field = new FieldDesignator(tag);
                    stack.push(field);
                    beginControlField(field);
                    break;
                }
                case "record": {
                    if (!inRecord) {
                        beginRecord(format != null ? format : "AlephPublish", type);
                        inRecord = true;
                    }
                    break;
                }
            }
        } else if (event.isCharacters()) {
            Characters c = (Characters) event;
            if (!c.isIgnorableWhiteSpace()) {
                if (sb.length() == 0) {
                    sb.append(" "); // for subfield ID 
                }
                sb.append(c.getData());
            }
        } else if (event.isEndElement()) {
            EndElement element = (EndElement) event;
            String localName = element.getName().getLocalPart();
            switch (localName) {
                case "subfield":
                    stack.peek().setData(sb.toString());
                    endSubField(stack.pop());
                    break;
                case "datafield":
                    // can't have data
                    endDataField(stack.pop());
                    break;
                case "controlfield":
                    stack.peek().setData(sb.toString());
                    endControlField(stack.pop());
                    break;
                case "leader":
                    leader(sb.toString());
                    break;
                case "record":
                    if (inRecord) {
                        endRecord();
                        inRecord = false;
                    }
                    break;
            }
            sb.setLength(0);
        }
    }

    private void createSession() throws IOException {
        this.connection = (Connection<SQLSession>) ConnectionManager.getConnection(uri, false, true);
        this.session = connection.createSession();
        session.open(Mode.READ);
        if (!session.isOpen()) {
            throw new IOException("session could not be opened");
        }
    }
}
