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
package org.xbib.elasticsearch;

import org.elasticsearch.action.get.GetRequestBuilder;
import org.xbib.elasticsearch.action.search.support.BasicGetRequest;
import org.xbib.logging.Logger;

import java.io.IOException;

public class GetRequest extends BasicGetRequest {

    @Override
    public GetRequest newRequest(GetRequestBuilder getRequestBuilder) {
        super.newRequest(getRequestBuilder);
        return this;
    }

    public GetResponse execute(Logger queryLogger) throws IOException {
        GetResponse response = new GetResponse();
        if (getRequestBuilder() == null) {
            queryLogger.debug("no get request");
            return response;
        }
        queryLogger.info(" get request: {}/{}/{}",
                getRequestBuilder().request().index(),
                getRequestBuilder().request().type(),
                getRequestBuilder().request().id());
        long t0 = System.currentTimeMillis();
        response.setResponse(getRequestBuilder().execute().actionGet());
        long t1 = System.currentTimeMillis();
        queryLogger.info(" get request complete: {}/{}/{} [{}ms] {}",
                getRequestBuilder().request().index(),
                getRequestBuilder().request().type(),
                getRequestBuilder().request().id(),
                (t1 - t0), response.exists());
        return response;
    }

}
