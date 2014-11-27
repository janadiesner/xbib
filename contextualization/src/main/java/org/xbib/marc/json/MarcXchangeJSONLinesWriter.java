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
package org.xbib.marc.json;

import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.marc.Field;
import org.xbib.marc.FieldList;
import org.xbib.marc.MarcException;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.transformer.StringTransformer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Convert a MarcXchange stream to Elasticsearch XContent lines
 */
public class MarcXchangeJSONLinesWriter implements MarcXchangeListener, MarcXchangeConstants {

    private final OutputStream out;

    private XContentBuilder builder;

    private FieldList fields;

    private StringTransformer transformer;

    private MarcXchangeListener marcXchangeListener;

    public MarcXchangeJSONLinesWriter(OutputStream out) throws IOException {
        this.out = out;
        this.builder = jsonBuilder(out);
    }

    public MarcXchangeJSONLinesWriter setMarcXchangeListener(MarcXchangeListener marcXchangeListener) {
        this.marcXchangeListener = marcXchangeListener;
        return this;
    }

    public MarcXchangeJSONLinesWriter setStringTransformer(StringTransformer transformer) {
        this.transformer = transformer;
        return this;
    }

    public void startDocument() throws IOException {
        // empty
    }

    public void endDocument() throws IOException {
        // empty
    }

    @Override
    public void beginCollection() {
        if (marcXchangeListener != null) {
            marcXchangeListener.beginCollection();
        }
    }

    @Override
    public void endCollection() {
        if (marcXchangeListener != null) {
            marcXchangeListener.endCollection();
        }
    }

    @Override
    public void beginRecord(String format, String type) {
        if (marcXchangeListener != null) {
            marcXchangeListener.beginRecord(format, type);
        }
        try {
            builder.startObject();
            if (format != null) {
                builder.field(FORMAT_TAG, format);
            }
            if (type != null) {
                builder.field(TYPE_TAG, type);
            }
        } catch (IOException e) {
            throw new MarcException(e);
        }
    }

    @Override
    public void endRecord() {
        if (marcXchangeListener != null) {
            marcXchangeListener.endRecord();
        }
        try {
            builder.endObject();
            builder.flush();
            out.write('\n');
        } catch (IOException e) {
            throw new MarcException(e);
        }
    }

    @Override
    public void leader(String label) {
        if (marcXchangeListener != null) {
            marcXchangeListener.leader(label);
        }
        try {
            if (label != null) {
                builder.field(LEADER_TAG, label);
            }
        } catch (IOException e) {
            throw new MarcException(e);
        }
    }

    @Override
    public void beginControlField(Field field) {
        if (marcXchangeListener != null) {
            marcXchangeListener.beginControlField(field);
        }
        fields = new FieldList();
        fields.add(field);
    }

    @Override
    public void endControlField(Field field) {
        if (marcXchangeListener != null) {
            marcXchangeListener.endControlField(field);
        }
        String data = field != null ? field.data() : null;
        if (transformer != null) {
            data = transformer.transform(data);
        }
        try {
            for (Field f : fields) {
                builder.field(f.tag(), data);
            }
        } catch (IOException e) {
            throw new MarcException(e);
        }
    }

    @Override
    public void beginDataField(Field field) {
        if (marcXchangeListener != null) {
            marcXchangeListener.beginDataField(field);
        }
        fields = new FieldList();
        fields.add(field);
    }

    @Override
    public void endDataField(Field field) {
        if (marcXchangeListener != null) {
            marcXchangeListener.endDataField(field);
        }
        String data = field != null ? field.data() : null;
        // if we have no subfields (data is in data field),
        // so move data to a subfield with subfield code "a"
        if (field != null && data != null && !data.isEmpty()) {
            field.subfieldId("a");
            endSubField(field);
        }
        try {
            Iterator<Field> it = fields.iterator();
            Field f = it.next();
            builder.startObject(f.tag());
            if (!f.indicator().isEmpty()) {
                builder.startObject(f.indicator().replace(' ', '_'));
            }
            while (it.hasNext()) {
                f = it.next();
                builder.field(f.subfieldId(), f.data());
            }
            if (!f.indicator().isEmpty()) {
                builder.endObject();
            }
            builder.endObject();
        } catch (IOException e) {
            throw new MarcException(e);
        }
    }

    @Override
    public void beginSubField(Field field) {
        if (marcXchangeListener != null) {
            marcXchangeListener.beginSubField(field);
        }
    }

    @Override
    public void endSubField(Field field) {
        if (marcXchangeListener != null) {
            marcXchangeListener.endSubField(field);
        }
        if (field == null) {
            return;
        }
        if (transformer != null) {
            field.data(transformer.transform(field.data()));
        }
        fields.add(field);
    }
}
