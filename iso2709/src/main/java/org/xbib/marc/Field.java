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

import org.xbib.io.field.FieldSortable;

/**
 * A field in ISO 2709 records.
 */
public class Field implements Comparable<Field> {

    public final static String ERROR_TAG = "___";

    public final static String NULL_TAG = "000";

    private final int position;

    private final int length;

    private String tag;

    private String indicator;

    private String subfieldId;

    private String data;

    private String sortable;

    public Field() {
        this(null, null, null);
    }

    public Field(String tag) {
        this(tag, null, null);
    }

    public Field(String tag, char ind1) {
        this(tag, Character.toString(ind1), null);
    }

    public Field(String tag, char ind1, char ind2) {
        this(tag, Character.toString(ind1) + Character.toString(ind2), null);
    }

    public Field(String tag, char ind1, char ind2, char code) {
        this(tag, Character.toString(ind1) + Character.toString(ind2),
                Character.toString(code));
    }

    public Field(String tag, String indicator) {
        this(tag, indicator, null);
    }

    public Field(String tag, String indicator, String subfieldId) {
        this.tag = tag;
        this.indicator = indicator;
        this.subfieldId = subfieldId;
        this.sortable = null;
        this.position = -1;
        this.length = -1;
    }

    public Field(String tag, int position, int length) {
        this.tag = tag;
        this.indicator = null;
        this.subfieldId = null;
        this.position = position;
        this.length = length;
    }

    public Field(Field field) {
        this.tag = field.tag();
        this.indicator = field.indicator();
        this.subfieldId = field.subfieldId();
        this.position = field.position();
        this.length = field.length();
        // data is undefined
    }

    /**
     * Create field, try to derive tag, indicators, and subfield ID from rawContent
     *
     * @param label the label
     * @param rawContent raw content
     */
    public Field(RecordLabel label, String rawContent) {
        this.tag = rawContent.length() > 2 ? rawContent.substring(0, 3) : ERROR_TAG;
        if (isControlField()) {
            this.indicator = null;
            this.subfieldId = null;
            data(rawContent.length() <= 3 ? rawContent.substring(3) : rawContent);
        } else {
            int indlen = label.getIndicatorLength();
            this.indicator = rawContent.length() > 2 + indlen ? rawContent.substring(3, 3 + indlen) : null;
            this.subfieldId = null; // no subfield
            data(rawContent.length() > 2 + indlen ? rawContent.substring(3 + indlen) : null);
        }
        this.position = -1;
        this.length = -1;
    }

    /**
     * Create field from a given designator and a subfield
     *
     * @param label label
     * @param designator designator
     * @param content content
     * @param withSubfield subfield
     */
    public Field(RecordLabel label, Field designator, String content, boolean withSubfield) {
        this.tag = designator.tag();
        this.position = designator.position();
        this.length = designator.length();
        int indlen = label.getIndicatorLength();
        int subfieldidlen = label.getSubfieldIdentifierLength();
        if (withSubfield) {
            this.indicator = designator.indicator();
            if (subfieldidlen > 1 && content.length() > subfieldidlen - 1) {
                this.subfieldId = content.substring(0, subfieldidlen - 1);
                data(content.substring(subfieldidlen - 1));
            } else {
                // no subfield identifier length specified or content is too short
                this.subfieldId = null;
                data(content);
            }
        } else {
            if (designator.isControlField()) {
                this.indicator = null;
                this.subfieldId = null;
                data(content);
            } else {
                this.indicator = indlen <= content.length() ? content.substring(0, indlen) : content;
                this.subfieldId = null;
                data(indlen <= content.length() ? content.substring(indlen) : null);
            }
        }
    }

    /**
     * Returns true if this field has a defined tag and data.
     * @return true if tag is not null and data is not null and data is not empty
     */
    public boolean isEmpty() {
        return tag == null || (data != null && data.isEmpty());
    }

    /**
     * Returns true if this field is a control field.
     * @return true if tag is between 000 and 009 (inclusive)
     */
    public final boolean isControlField() {
        return tag != null && tag.charAt(0) == '0' && tag.charAt(1) == '0';
    }

    /**
     * Returns true if this field has an indicator
     * @return true if indicators exist, false if not
     */
    public boolean hasIndicator() {
        return indicator != null;
    }

    /**
     * Returns true if this field is a subfield
     * @return true is
     */
    public boolean isSubField() {
        return subfieldId != null;
    }

    /**
     * Set a tag for this field.
     *
     * @param tag a tag
     * @return this field
     */
    public Field tag(String tag) {
        this.tag = tag;
        return this;
    }

    /**
     * Get this field's tag
     *
     * @return tag
     */
    public String tag() {
        return tag;
    }

    /**
     * Set a sequence of indicators for this field
     *
     * @param indicator the sequence of indicators
     * @return this field
     */
    public Field indicator(String indicator) {
        this.indicator = indicator;
        return this;
    }

    /**
     * Get indicator sequence.
     *
     * @return the indicator sequence
     */
    public String indicator() {
        return indicator;
    }

    /**
     * Set the sub field identifier.
     *
     * @param subfieldId the subfield identifier
     * @return this Field object
     */
    public Field subfieldId(String subfieldId) {
        this.subfieldId = subfieldId;
        return this;
    }

    /**
     * Get subfield identifier
     *
     * @return the subfield identifier
     */
    public String subfieldId() {
        return subfieldId;
    }

    /**
     * The position of the field in the record. The position
     * unit is measured in octets.
     *
     * @return the field position
     */
    public int position() {
        return position;
    }

    /**
     * Return the length of the field in the record. The
     * field length is measured in octets (or "bytes").
     *
     * @return the field length
     */
    public int length() {
        return length;
    }

    /**
     * Set data for a data field.
     *
     * @param data the data
     * @return this Field object
     */
    public Field data(String data) {
        this.data = data;
        this.sortable = data != null && data.indexOf(FieldSortable.NON_SORTABLE_BEGIN) >= 0 ?
                data.replaceAll(FieldSortable.NON_SORTABLE_BEGIN + ".*?" + FieldSortable.NON_SORTABLE_END, "") : null;
        return this;
    }

    /**
     * Get the field data.
     *
     * @return the data
     */
    public String data() {
        return data;
    }

    /**
     * Get sortable field data
     * @return the sortable data
     */
    public String dataSortable() {
        return sortable;
    }

    protected String getDesignator() {
        return tag + (indicator != null ? indicator : "") + (subfieldId != null ? subfieldId : "");
    }

    @Override
    public String toString() {
        return tag + (indicator != null ? indicator : "") + (subfieldId != null ? subfieldId : "") + (data != null ? "=" + data : "");
    }

    @Override
    public int compareTo(Field o) {
        return getDesignator().compareTo(o.getDesignator());
    }
}
