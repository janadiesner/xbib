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
package org.xbib.elements.marc;

import org.xbib.elements.KeyValueElementPipeline;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.rdf.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A MARC pipeline is a key/value pipeline specifically designed for MARC mapping.
 * It performs the heavy lifting by looking up the structured information in the Element Map
 * and processes the MARC fields.
 *
 */
public class MARCElementPipeline extends KeyValueElementPipeline<FieldCollection, String, MARCElement, MARCContext> {

    private final Logger logger = LoggerFactory.getLogger(MARCElementPipeline.class.getName());

    public MARCElementPipeline(int i) {
        super(i);
    }

    @Override
    protected void build(FieldCollection fields, String value) {
        if (fields == null) {
            return;
        }
        String key = fields.toSpec();
        MARCElement element = null;
        try {
            element = (MARCElement) specification.getElement(key, map());
            // if nothing was found, try tag only
            if (element == null) {
                element = (MARCElement) specification.getElementBySpec(key, map());
            }
        } catch (ClassCastException e) {
            logger.error("not a MARCElement instance for key" + key);
        }
        if (element != null) {
            // element-based processing
            boolean done = element.fields(this, builder(), fields, value);
            if (done) {
                return;
            }
            addToResource(builder().context().getResource(), fields, element, value);
            // build other things like facets
            builder().build(element, fields, value);
        } else {
            if (detectUnknownKeys) {
                unknownKeys.add(key);
                if (logger.isDebugEnabled()) {
                    logger.debug("unknown key detected: {}", fields);
                }
            }
        }
        builder().build(element, fields, value);
    }

    public void addToResource(Resource resource,
                              FieldCollection fields,
                              MARCElement element,
                              String value) {
        // setup
        Map<String, Object> defaultSubfields = (Map<String, Object>) element.getSettings().get("subfields");
        if (defaultSubfields == null) {
            return;
        }
        Map<Field, String> fieldNames = new HashMap();
        // create another anoymous resource
        Resource newResource = builder().context().newResource();
        // default predicate is the name of the element class
        String predicate = element.getClass().getSimpleName();
        // the _predicate field allows to select a field to name the resource by a coded value
        if (element.getSettings().containsKey("_predicate")) {
            predicate = (String) element.getSettings().get("_predicate");
        }
        boolean overridePredicate = false;
        // put all found fields with configured subfield names to this resource
        for (Field field : fields) {
            Map<String, Object> subfields = defaultSubfields;
            // tag predicates defined?
            if (element.getSettings().containsKey("tags")) {
                Map<String, Object> tags = (Map<String, Object>) element.getSettings().get("tags");
                if (tags.containsKey(field.tag())) {
                    if (!overridePredicate) {
                        predicate = (String) tags.get(field.tag());
                    }
                    subfields = (Map<String, Object>) element.getSettings().get(predicate);
                    if (subfields == null) {
                        subfields = defaultSubfields;
                    }
                }
            }
            // indicator-based predicate defined?
            if (element.getSettings().containsKey("indicators")) {
                Map<String, Object> indicators = (Map<String, Object>) element.getSettings().get("indicators");
                if (indicators.containsKey(field.tag())) {
                    Map<String,Object> indicatorMap =  (Map<String,Object>)indicators.get(field.tag());
                    if (indicatorMap.containsKey(field.indicator())) {
                        if (!overridePredicate) {
                            predicate = (String) indicatorMap.get(field.indicator());
                            fieldNames.put(field, predicate);
                        }
                        subfields = (Map<String, Object>) element.getSettings().get(predicate);
                        if (subfields == null) {
                            subfields = defaultSubfields;
                        }
                    }
                }
            }

            // is there a subfield value decoder?
            Map.Entry<String, Object> me = SubfieldValueMapper.map(subfields, field);
            if (me.getKey() != null) {
                String v = me.getValue().toString();
                if (fieldNames.containsKey(field)) {
                    String fieldName = fieldNames.get(field);
                    // field-specific subfield map
                    Map<String, Object> vm =
                            (Map<String, Object>) element.getSettings().get(fieldName);
                    if (vm == null) {
                        // fallback to "subfields"
                        vm =  (Map<String, Object>) element.getSettings().get("subfields");
                    }
                    int pos = v.indexOf(' '); // code must be non-blank word
                    String vv = pos > 0 ? v.substring(0, pos) : v;
                    // code table lookup
                    if (vm.containsKey(v)) {
                        newResource.add(me.getKey() + "Source", v);
                        v = (String) vm.get(v);
                    } else if (vm.containsKey(vv)) {
                        newResource.add(me.getKey() + "Source", v);
                        v = (String) vm.get(vv);
                    } else {
                        List<Map<String, String>> patterns = (List<Map<String, String>>) element.getSettings().get(fieldName + "pattern");
                        if (patterns != null) {
                            for (Map<String, String> pattern : patterns) {
                                Map.Entry<String, String> mme = pattern.entrySet().iterator().next();
                                String p = mme.getKey();
                                String rel = mme.getValue();
                                Matcher m = Pattern.compile(p, Pattern.CASE_INSENSITIVE).matcher(v);
                                if (m.matches()) {
                                    newResource.add(me.getKey() + "Source", v);
                                    v = rel;
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    // default subfield map
                    String fieldName = me.getKey();
                    if (element.getSettings().containsKey(fieldName)) {
                        Map<String, Object> vm = (Map<String, Object>) element.getSettings().get(fieldName);
                        int pos = v.indexOf(' ');
                        String vv = pos > 0 ? v.substring(0, pos) : v;
                        if (vm.containsKey(v)) {
                            newResource.add(me.getKey() + "Source", v);
                            v = (String) vm.get(v);
                        } else if (vm.containsKey(vv)) {
                            newResource.add(me.getKey() + "Source", v);
                            v = (String) vm.get(vv);
                        } else {
                            // relation by pattern?
                            List<Map<String, String>> patterns = (List<Map<String, String>>) element.getSettings().get(fieldName + "pattern");
                            if (patterns != null) {
                                for (Map<String, String> pattern : patterns) {
                                    Map.Entry<String, String> mme = pattern.entrySet().iterator().next();
                                    String p = mme.getKey();
                                    String rel = mme.getValue();
                                    Matcher m = Pattern.compile(p, Pattern.CASE_INSENSITIVE).matcher(v);
                                    if (m.matches()) {
                                        newResource.add(me.getKey() + "Source", v);
                                        v = rel;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                // transform value v
                v = element.data(builder(), predicate, newResource, me.getKey(), v);
                // is this the predicate field or a value?
                if (me.getKey().equals(predicate)) {
                    predicate = v;
                    overridePredicate = true;
                } else {
                    newResource.add(me.getKey(), v);
                }
            } else {
                // no decoder, simple add field data
                String property = null;
                String subfieldId = field.subfieldId();
                if (subfieldId == null) {
                    subfieldId = ""; // empty string, for map lookup
                }
                try {
                    property = (String) subfields.get(subfieldId);
                } catch (ClassCastException e) {
                    logger.error("cannot use string property of '" + subfieldId + "' for field " + field);
                }
                if (property == null) {
                    // unmapped subfield ID
                    property = subfieldId;
                }
                newResource.add(property, element.data(builder(), predicate, newResource, property, field.data()));
            }
            // call custom add-ons
            element.field(builder(), field, value);
        }
        // add child resource
        resource.add(predicate, newResource);
    }


}
