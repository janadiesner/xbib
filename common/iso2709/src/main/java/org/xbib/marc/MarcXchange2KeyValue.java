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

import org.xbib.io.keyvalue.KeyValueStreamListener;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Convert a MarcXchange stream to a key/value stream. With optional value
 * string transformation.
 */
public class MarcXchange2KeyValue implements MarcXchangeListener,
        KeyValueStreamListener<FieldCollection, String> {

    public interface FieldDataTransformer {

        String transform(String value);
    }

    private FieldCollection fields;

    private List<KeyValueStreamListener<FieldCollection, String>> listeners =
            new LinkedList<KeyValueStreamListener<FieldCollection, String>>();

    private FieldDataTransformer transformer;

    public MarcXchange2KeyValue addListener(KeyValueStreamListener<FieldCollection, String> listener) {
        this.listeners.add(listener);
        return this;
    }

    public MarcXchange2KeyValue transformer(FieldDataTransformer transformer) {
        this.transformer = transformer;
        return this;
    }

    @Override
    public KeyValueStreamListener<FieldCollection, String> begin() throws IOException {
        for (KeyValueStreamListener<FieldCollection, String> listener : listeners) {
            listener.begin();
        }
        return this;
    }

    @Override
    public KeyValueStreamListener<FieldCollection, String> keyValue(FieldCollection key, String value) throws IOException {
        for (KeyValueStreamListener<FieldCollection, String> listener : listeners) {
            // we allow null value, but no null keys to be passed to the listeners
            if (key != null) {
                listener.keyValue(key, value);
            }
        }
        return this;
    }

    @Override
    public KeyValueStreamListener<FieldCollection, String> keys(List<FieldCollection> keys) throws IOException {
        return this;
    }

    @Override
    public KeyValueStreamListener<FieldCollection, String> values(List<String> values) throws IOException {
        return this;
    }

    @Override
    public KeyValueStreamListener<FieldCollection, String> end() throws IOException {
        for (KeyValueStreamListener<FieldCollection, String> listener : listeners) {
            listener.end();
        }
        return this;
    }

    @Override
    public void beginRecord(String format, String type) {
        try {
            begin();
            keyValue(FieldCollection.FORMAT_KEY, format);
            keyValue(FieldCollection.TYPE_KEY, type);
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
            keyValue(FieldCollection.LEADER_KEY, label);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beginControlField(Field field) {
        fields = new FieldCollection();
        fields.add(field);
    }

    @Override
    public void endControlField(Field field) {
        String data = field != null ? field.data() : null;
        // transform field data?
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
        fields = new FieldCollection();
        fields.add(field);
    }

    @Override
    public void endDataField(Field field) {
        // put data into the emitter if the only have one field
        String data = field != null ? field.data() : null;
        if (data == null && fields.size() == 1) {
            data = fields.getFirst().data();
        }
        // transform field data?
        if (transformer != null && data != null) {
            data = transformer.transform(data);
        }
        // emit fields as key/value
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
        if (field != null) {
            // remove last field if there is no sub field (it must be a data field)
            if (!fields.isEmpty() && !fields.getLast().isSubField()) {
                fields.removeLast();
            }            
            // transform field data?
            if (transformer != null && field.data() != null) {
                field.data(transformer.transform(field.data()));
            }
            fields.add(field);
        }
    }
}
