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
package org.xbib.objectstorage.action;

import org.xbib.objectstorage.Request;
import org.xbib.objectstorage.Response;
import org.xbib.util.ILL;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ItemHeadAction extends AbstractQueryAction {

    public ItemHeadAction(String sql) {
        super(sql);
    }

    @Override
    protected String[] createBindKeys() {
        return new String[]{"id", "isil"};
    }

    @Override
    protected Map<String, Object> createParams(Request request) throws IOException {
        ILL ill = new ILL().set(request.getItem());
        ill.normalize().verify();
        long id = ill.getNumber();
        final Map<String, Object> params = new HashMap();
        params.put("id", id);
        params.put(NAME_PARAMETER, request.getUserAttributes().getName());
        logger.debug("item head action = {} params = {}", sql, params);
        return params;
    }

    @Override
    protected int buildResponse(ResultSet result, Request request, Response response)
            throws SQLException {
        ResultSetMetaData m = result.getMetaData();
        for (int i = 1; i <= m.getColumnCount(); i++) {
            response.builder().header("X-Metadata-" + m.getColumnName(i).toLowerCase(), result.getString(i));
        }
        return -1;
    }

}
