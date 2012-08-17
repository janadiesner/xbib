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
package org.xbib.sru.adapter;

import java.io.IOException;
import org.xbib.io.http.netty.HttpOperation;
import org.xbib.query.cql.SyntaxException;
import org.xbib.sru.Scan;
import org.xbib.sru.ScanResponse;
import org.xbib.sru.SearchRetrieve;
import org.xbib.sru.SearchRetrieveResponse;
import org.xbib.sru.client.SimpleSRUClient;
import org.xbib.xml.transform.StylesheetTransformer;

public abstract class ProxySRUAdapter extends AbstractSRUAdapter {

    @Override
    public void performSearchRetrieve(SearchRetrieve request, SearchRetrieveResponse response, 
            StylesheetTransformer transformer)
            throws IOException, SyntaxException {
        response.addResponseParameter("X-SRU-version",
                request.getVersion());
        response.addResponseParameter("X-SRU-recordSchema",
                request.getRecordSchema());
        response.addResponseParameter("X-SRU-recordPacking",
                request.getRecordPacking());
        response.addResponseParameter("X-SRU-origin",
                request.getURI() != null ? request.getURI().toASCIIString() : "undefined");
        SimpleSRUClient client = new SimpleSRUClient();
        client.setStylesheetTransformer(transformer);
        HttpOperation op = client.searchRetrieve(request, response);
        getLogger().info("[response={}ms] [uri={}] [status={}] [contenttype={}] [query={}]",
                // TODO total number of results
                op.getResponseMillis(),
                request.getURI(), 
                op.getResults().get(request.getURI()).getStatusCode(), 
                op.getContentType(request.getURI()), 
                request.getQuery());
    }

    @Override
    public void performScan(Scan request, ScanResponse response) {
        // todo
    }
    
}