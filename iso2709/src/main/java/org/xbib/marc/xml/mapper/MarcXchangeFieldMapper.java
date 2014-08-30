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
package org.xbib.marc.xml.mapper;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.RecordLabel;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The MarcXchange field mapper parses MarcXchange fields one by one,
 * with the capability to map fields to other ones.
 */
public abstract class MarcXchangeFieldMapper implements MarcXchangeListener {

    private static final Logger logger = LoggerFactory.getLogger(MarcXchangeFieldMapper.class.getName());

    private FieldCollection record = new FieldCollection();

    private FieldCollection controlfields = new FieldCollection();

    private FieldCollection datafields = new FieldCollection();

    private Field previousField;

    private int repeatCounter;

    private String format;

    private String type;

    private String label;

    private Map<String, Object> map;

    public MarcXchangeFieldMapper setFieldMap(Map<String, Object> map) {
        this.map = map;
        return this;
    }

    public Map<String, Object> getFieldMap() {
        return map;
    }

    public MarcXchangeFieldMapper setFormat(String format) {
        this.format = format;
        return this;
    }

    public MarcXchangeFieldMapper setType(String type) {
        this.type = type;
        return this;
    }

    public String getFormat() {
        return format;
    }

    public String getType() {
        return type;
    }

    protected void setRecordLabel(String label) {
        RecordLabel recordLabel = new RecordLabel(label.toCharArray());
        this.label = recordLabel.getRecordLabel();
    }

    protected void addControlField(Field field) {
        this.controlfields.add(field);
    }

    protected void addDataField(Field field) {
        this.datafields.add(field);
    }

    protected FieldCollection getControlFields() {
        return controlfields;
    }

    protected FieldCollection getDataFields() {
        return datafields;
    }

    protected void flushField() {
        if (datafields == null) {
            return;
        }
        // data fields
        Iterator<Field> it = datafields.iterator();
        if (!it.hasNext()) {
            return;
        }
        Field dataField = it.next();
        // is this tag repeated?
        if (previousField != null && isRepeat(previousField, dataField)) {
            repeatCounter++;
        } else {
            repeatCounter = 0;
        }
        // save field
        previousField = new Field(dataField);
        // the heavy lifting, map the field
        Field mappedField = map(dataField);
        boolean mapped = !dataField.getDesignator().equals(mappedField.getDesignator());
        // open a new data field.
        // if mapped, check if this field is not the same as the last field in the record
        if (record.isEmpty()) {
            record.add(mappedField); // this is the first field
        } else if (mapped) {
            // field was mappped. The rule is that subfields must not repeat and must be continous. If not, close data field.
            if (!isContinous(record.getLast(), mappedField)) {
                // not same field tag, close old data field and add new data field
                record.add(Field.EMPTY);
            }
            record.add(mappedField);
        } else {
            // not mapped
            record.add(dataField);
        }
        // loop over fields and map them all
        while (it.hasNext()) {
            Field field = it.next();
            if (Field.EMPTY.equals(field)) {
                record.add(field);
            } else {
                record.add(map(field));
            }
        }
        // reset datafields
        datafields = new FieldCollection();
    }

    protected void flushRecord() {
        // make sure there is no field left over
        flushField();
        // skip empty record
        if (record == null || record.isEmpty()) {
            return;
        }
        beginRecord(format, type);
        leader(label);
        boolean inData = false;
        for (Field field : controlfields) {
            beginControlField(field);
            endControlField(field);
        }
        // Sequence of data fields is:
        // 1. datafield (open) - optional
        // 2. subfield list of datafield
        // 3. datafield (close)
        // Control fields are skipped.
        for (Field field : record) {
            if (field.isSubField()) {
                if (!inData) {
                    beginDataField(new Field().tag(field.tag()).indicator(field.indicator()));
                    inData = true;
                }
                beginSubField(field);
                endSubField(field);
            } else if (!field.isControlField()) {
                if (inData || Field.EMPTY.equals(field)) {
                    endDataField(field.data(""));
                    inData = false;
                } else {
                    beginDataField(field);
                    inData = true;
                }
            }
        }
        // broken field data structure?
        if (inData) {
            endDataField(Field.EMPTY);
        }
        endRecord();
        // reset all the counters and variables for next record
        repeatCounter = 0;
        previousField = null;
        datafields = new FieldCollection();
        controlfields = new FieldCollection();
        record = new FieldCollection();
    }

    /**
     * The mapper. Maps a field by the following convention:
     *
     * tag : {
     *   ind : {
     *       subf : "totag$toind$tosubf"
     *   }
     * }
     *
     * where toind can be interpolated by repeat counter.
     *
     * @param field the field to map from
     * @return the mpped field
     */
    @SuppressWarnings("unchecked")
    protected Field map(Field field) {
        if (map == null) {
            return field;
        }
        if (field == null) {
            return null;
        }
        if (map.containsKey(field.tag())) {
            Object o = map.get(field.tag());
            if (o instanceof Map) {
                if (field.isControlField()) {
                    Map<String, Object> subf = (Map<String, Object>) o;
                    if (subf.containsKey("")) {
                        o = subf.get("");
                        if (o != null) {
                            String[] s = o.toString().split("\\$");
                            if (s.length >= 2) {
                                s[1] = interpolate(s[1]);
                                field.tag(s[0]).indicator(s[1]);
                            } else if (s.length == 1) {
                                field.tag(s[0]);
                            }
                        }
                    }
                } else {
                    Map<String, Object> ind = (Map<String, Object>) o;
                    if (ind.containsKey(field.indicator())) {
                        o = ind.get(field.indicator());
                        if (o instanceof Map) {
                            Map<String, Object> subf = (Map<String, Object>) o;
                            String subfieldId = field.isSubField() ? field.subfieldId() : "";
                            if (subf.containsKey(subfieldId)) {
                                o = subf.get(subfieldId);
                                if (o != null) {
                                    String[] s = o.toString().split("\\$");
                                    if (s.length >= 2) {
                                        s[1] = interpolate(s[1]);
                                        // subfield -> subfield, data field -> data field
                                        if (field.isSubField()) {
                                            field.tag(s[0]).indicator(s[1]).subfieldId(s[2]);
                                        } else {
                                            field.tag(s[0]).indicator(s[1]);
                                        }
                                    } else if (s.length == 1) {
                                        field.tag(s[0]);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return field;
    }

    /**
     * Checks if a field repeats another.
     * @param previous the previous field
     * @param next the next field
     * @return true if field is repeated
     */
    protected boolean isRepeat(Field previous, Field next) {
        return previous.tag().equals(next.tag());
    }

    /**
     * Checks if field is continuous to the previous field. This means subfield inclusion.
     * @param previous the previous field
     * @param next the next field
     * @return true if field is continuous
     */
    protected boolean isContinous(Field previous, Field next) {
        return isRepeat(previous, next)
                && ((!previous.isSubField() && !next.isSubField()) ||
                (previous.isSubField() && next.isSubField() && previous.subfieldId().compareTo(next.subfieldId()) <= 0));
    }

    // the repeat counter pattern
    private final Pattern REP = Pattern.compile("\\{r\\}");

    /**
     * Interpolate variables.
     * @param value the input value
     * @return the interpolated string
     */
    protected String interpolate(String value) {
        Matcher m = REP.matcher(value);
        if (m.find()) {
            return m.replaceAll(Integer.toString(repeatCounter));
        } else {
            return value;
        }
    }

}
