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
package org.xbib.elements.marc.direct;

import org.xbib.elements.direct.KeyValueDirectPipeline;
import org.xbib.elements.marc.MARCContext;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.rdf.Resource;

public class MARCDirectPipeline extends KeyValueDirectPipeline<FieldCollection, String, MARCContext> {

    public MARCDirectPipeline(int i) {
        super(i);
    }

    @Override
    protected void build(FieldCollection fields, String value) {
        if (fields == null) {
            return;
        }
        String tag = fields.iterator().next().tag();
        Resource tagResource = builder().context().newResource();
        for (Field field : fields) {
            if ("001".equals(field.tag())) {
                builder().context().setRecordNumber(field.data());
            }
            if (field.isControlField()) {
                tagResource.add("_", field.data());
            } else {
                if (field.hasIndicator()) {
                    // with indicator
                    String predicate = field.indicator().replaceAll("\\s", "_");
                    tagResource.newResource(predicate)
                        .add(field.isSubField() ? field.subfieldId() : "_", field.data());
                } else {
                    // without indicator
                    tagResource.add(field.isSubField() ? field.subfieldId() : "_", field.data());
                }
            }
        }
        builder().context().getResource().add(tag, tagResource);
    }

}
