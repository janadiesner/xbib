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

import org.xbib.elements.ElementBuilderFactory;
import org.xbib.elements.ElementMap;
import org.xbib.elements.KeyValuePipeline;
import org.xbib.keyvalue.KeyValue;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.rdf.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class MARCPipeline
        extends KeyValuePipeline<FieldCollection, String, MARCElement, MARCContext> {

    private final Logger logger = LoggerFactory.getLogger(MARCPipeline.class.getName());

    public MARCPipeline(int i,
                        BlockingQueue<List<KeyValue>> queue,
                        Map map,
                        ElementBuilderFactory<FieldCollection, String, MARCElement, MARCContext> factory) {
        super(i, queue, map, factory);
    }

    @Override
    protected void build(FieldCollection fields, String value) {
        if (fields == null) {
            return;
        }
        String key = fields.toString();
        MARCElement element = null;
        try {
            element = (MARCElement) ElementMap.getElement(key, map());
        } catch (ClassCastException e) {
            logger.error("not a MARCElement instance for key " + key);
        }
        if (element != null) {
            // element-based processing
            element.fields(builder(), fields, value);
            Map<String, Object> tags = (Map<String, Object>) element.getSettings().get("tags");
            Map<String, Object> subfields = (Map<String, Object>) element.getSettings().get("subfields");
            if (subfields != null) {
                Map<String, Object> defaultSubfields = subfields;
                // optional indicator configuration
                Map<String, Object> indicators = (Map<String, Object>) element.getSettings().get("indicators");
                Map<Field, String> fieldNames = new HashMap();
                if (indicators != null) {
                    for (Field field : fields) {
                        Map.Entry<String, Object> me = TagValueMapper.map(indicators, field);
                        if (me.getKey() != null) {
                            fieldNames.put(field, (String) me.getValue());
                        }
                    }
                }
                // create new anoymous resource
                Resource resource = builder().context().resource();
                Resource newResource = builder().context().newResource();
                // default predicate is the name of the element class
                String predicate = element.getClass().getSimpleName();
                // the _predicate field allows to select a field to name the resource by a coded value
                if (element.getSettings().containsKey("_predicate")) {
                    predicate = (String) element.getSettings().get("_predicate");
                }
                boolean predicateFound = false;
                // put all found fields with configured subfield names to this resource
                for (Field field : fields) {
                    subfields = defaultSubfields;
                    // tag predicates?
                    if (element.getSettings().containsKey("tags")) {
                        if (tags.containsKey(field.tag())) {
                            if (!predicateFound) {
                                predicate = (String) tags.get(field.tag());
                            }
                            subfields = (Map<String, Object>) element.getSettings().get(predicate);
                            if (subfields == null) {
                                subfields = defaultSubfields;
                            }
                        }
                    }
                    // is there a subfield value decoder?
                    Map.Entry<String, Object> me = SubfieldValueMapper.map(subfields, field);
                    if (me.getKey() != null) {
                        String v = me.getValue().toString();
                        if (fieldNames.containsKey(field)) {
                            // field-specific subfield map?
                            Map<String, Object> vm = (Map<String, Object>) element.getSettings().get(fieldNames.get(field));
                            v = vm.containsKey(v) ? vm.get(v).toString() : v;
                        } else {
                            // default subfield map
                            if (element.getSettings().containsKey(me.getKey())) {
                                Map<String, Object> vm = (Map<String, Object>) element.getSettings().get(me.getKey());
                                v = vm.containsKey(v) ? vm.get(v).toString() : v;
                            }
                        }
                        // is this the predicate field or a value?
                        v = element.data(predicate, me.getKey(), v);
                        if (me.getKey().equals(predicate)) {
                            predicate = v;
                            predicateFound = true;
                        } else {
                            newResource.add(me.getKey(), v);
                        }
                    } else {
                        // no decoder, simple add field data
                        String property = null;
                        try {
                            property = (String) subfields.get(field.subfieldId());
                        } catch (ClassCastException e) {
                            logger.error("cannot use string property of '" + field.subfieldId() + "' for field " + field);
                        }
                        if (property == null) {
                            property = field.subfieldId(); // unmapped subfield ID
                        }
                        newResource.add(property, element.data(predicate, property, field.data()));
                    }
                    element.field(builder(), field, value);
                }
                // add child resource
                resource.add(predicate, newResource);
                builder().context().newResource(resource); // switch back to old resource
            }
        } else {
            if (detectUnknownKeys) {
                unknownKeys.add(key);
                if (logger.isDebugEnabled()) {
                    logger.debug("unknown key detected: {} {}", fields, value);
                }
            }
        }
        builder().build(element, fields, value);
    }

}