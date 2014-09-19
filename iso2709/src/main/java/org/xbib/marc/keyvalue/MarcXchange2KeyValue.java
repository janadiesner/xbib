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
package org.xbib.marc.keyvalue;

import org.xbib.keyvalue.KeyValueStreamListener;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.DataField;
import org.xbib.marc.Field;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.transformer.StringTransformer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Convert a MarcXchange stream to a key/value stream
 */
public class MarcXchange2KeyValue implements MarcXchangeListener, KeyValueStreamListener<DataField, String> {

    private static final Logger logger = LoggerFactory.getLogger(MarcXchange2KeyValue.class.getName());

    private DataField fields;

    private StringTransformer transformer;

    private List<KeyValueStreamListener<DataField, String>> listeners =
            new LinkedList<KeyValueStreamListener<DataField, String>>();

    public MarcXchange2KeyValue addListener(KeyValueStreamListener<DataField, String> listener) {
        this.listeners.add(listener);
        return this;
    }

    public MarcXchange2KeyValue setStringTransformer(StringTransformer transformer) {
        this.transformer = transformer;
        return this;
    }

    @Override
    public KeyValueStreamListener<DataField, String> begin() throws IOException {
        for (KeyValueStreamListener<DataField, String> listener : listeners) {
            listener.begin();
        }
        return this;
    }

    @Override
    public KeyValueStreamListener<DataField, String> keyValue(DataField key, String value) throws IOException {
        for (KeyValueStreamListener<DataField, String> listener : listeners) {
            // we allow null value, but null keys are not passed to the listeners
            if (key != null) {
                listener.keyValue(key, value);
            }
        }
        return this;
    }

    @Override
    public KeyValueStreamListener<DataField, String> keys(List<DataField> keys) throws IOException {
        return this;
    }

    @Override
    public KeyValueStreamListener<DataField, String> values(List<String> values) throws IOException {
        return this;
    }

    @Override
    public KeyValueStreamListener<DataField, String> end() throws IOException {
        for (KeyValueStreamListener<DataField, String> listener : listeners) {
            listener.end();
        }
        return this;
    }

    @Override
    public void beginCollection() {
    }

    @Override
    public void endCollection() {
    }

    @Override
    public void beginRecord(String format, String type) {
        try {
            begin();
            if (format != null) {
                keyValue(DataField.FORMAT_KEY, format);
            }
            if (type != null) {
                keyValue(DataField.TYPE_KEY, type);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void endRecord() {
        try {
            end();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void leader(String label) {
        try {
            if (label != null) {
                keyValue(DataField.LEADER_KEY, label);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beginControlField(Field field) {
        fields = new DataField();
        fields.add(field);
    }

    @Override
    public void endControlField(Field field) {
        String data = field != null ? field.data() : null;
        if (transformer != null && data != null) {
            data = transformer.transform(data);
        }
        try {
            keyValue(fields, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beginDataField(Field field) {
        fields = new DataField();
        fields.add(field);
    }

    @Override
    public void endDataField(Field field) {
        String data = field != null ? field.data() : null;
        try {
            keyValue(fields, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beginSubField(Field field) {
        // do nothing
    }

    @Override
    public void endSubField(Field field) {
        if (field == null) {
            return;
        }
        // transform field data
        if (transformer != null && field.data() != null) {
            field.data(transformer.transform(field.data()));
        }
        fields.add(field);
    }
}