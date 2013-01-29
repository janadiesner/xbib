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
package org.xbib.elasticsearch.support;

import java.io.IOException;
import java.io.StringReader;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.common.unit.TimeValue;
import org.xbib.logging.Logger;
import org.xbib.query.cql.CQLParser;
import org.xbib.query.cql.elasticsearch.ESGenerator;

public class ElasticsearchRequest {

    private SearchRequestBuilder searchRequestBuilder;
    private GetRequestBuilder getRequest;
    private ESGenerator generator;
    private String[] index;
    private String[] type;
    private String id;
    private String originalQuery;
    private String query;

    public ElasticsearchRequest newRequest(SearchRequestBuilder searchRequestBuilder) {
        this.searchRequestBuilder = searchRequestBuilder;
        this.generator = new ESGenerator();
        return this;
    }

    public ElasticsearchRequest newRequest(GetRequestBuilder getRequest) {
        this.getRequest = getRequest;
        return this;
    }

    public ElasticsearchRequest index(String index) {
        if (index != null && !"*".equals(index)) {
            this.index = new String[]{index};
        }
        return this;
    }

    public ElasticsearchRequest index(String... index) {
        this.index = index;
        return this;
    }

    public String index() {
        return index[0];
    }

    public ElasticsearchRequest type(String type) {
        if (type != null && !"*".equals(type)) {
            this.type = new String[]{type};
        }
        return this;
    }

    public ElasticsearchRequest type(String... type) {
        this.type = type;
        return this;
    }

    public String type() {
        return type[0];
    }

    public ElasticsearchRequest id(String id) {
        this.id = id;
        return this;
    }

    public String id() {
        return id;
    }

    public ElasticsearchRequest from(int from) {
        searchRequestBuilder.setFrom(from);
        generator.setFrom(from);
        return this;
    }

    public ElasticsearchRequest size(int size) {
        searchRequestBuilder.setSize(size);
        generator.setSize(size);
        return this;
    }

    public ElasticsearchRequest filter(String filter) {
        searchRequestBuilder.setFilter(filter);
        return this;
    }

    public ElasticsearchRequest facets(String facets) {
        searchRequestBuilder.setFacets(facets.getBytes());
        return this;
    }

    public ElasticsearchRequest timeout(TimeValue timeout) {
        searchRequestBuilder.setTimeout(timeout);
        return this;
    }


    public ElasticsearchRequest query(String query) {
        this.originalQuery = this.query;
        this.query = query == null || query.trim().length() == 0 ? "{\"query\":{\"match_all\":{}}}" : query;
        return this;
    }

    public ElasticsearchRequest cql(String query) throws IOException {
        if (query == null || query.trim().length() == 0) {
            from(0).size(10).query(null);
            return this;
        }
        this.originalQuery = query;
        CQLParser parser = new CQLParser(new StringReader(query));
        parser.parse();
        parser.getCQLQuery().accept(generator);
        this.query = generator.getRequestResult();
        return this;
    }

    public ElasticsearchResponse execute(Logger queryLogger)
            throws IOException {
        ElasticsearchResponse response = new ElasticsearchResponse();
        if (searchRequestBuilder == null) {
            return response;
        }
        if (query == null) {
            return response;
        }
        if (hasIndex(index)) {
            searchRequestBuilder.setIndices(fixIndexName(index));
        }
        if (hasType(type)) {
            searchRequestBuilder.setTypes(type);
        }
        long t0 = System.currentTimeMillis();
        response.searchResponse(searchRequestBuilder
                .setExtraSource(query)
                .execute().actionGet());
        long t1 = System.currentTimeMillis();
        if (queryLogger != null) {
            queryLogger.info(" [{}] [{}ms] [{}ms] [{}] [{}] [{}]",
                    formatIndexType(), t1 - t0, response.tookInMillis(), response.totalHits(), originalQuery, query);
        }
        // default format: JSON
        response.format(OutputFormat.JSON);
        return response;
    }

    public ElasticsearchResponse executeGet(Logger queryLogger) throws IOException {
        ElasticsearchResponse response = new ElasticsearchResponse();
        long t0 = System.currentTimeMillis();
        response.getResponse(getRequest.execute().actionGet());
        long t1 = System.currentTimeMillis();
        if (queryLogger != null) {
            queryLogger.info(" get complete: {}/{}/{} [{}ms] {}",
                    index, type, getRequest.request().id(), (t1 - t0), response.exists());
        }
        // default format: JSON
        response.format(OutputFormat.JSON);
        return response;
    }

    private boolean hasIndex(String[] s) {
        if (s == null) {
            return false;
        }
        if (s.length == 0) {
            return false;
        }
        if (s[0] == null) {
            return false;
        }
        return true;
    }

    private boolean hasType(String[] s) {
        if (s == null) {
            return false;
        }
        if (s.length == 0) {
            return false;
        }
        if (s[0] == null) {
            return false;
        }
        return true;
    }

    private String[] fixIndexName(String[] s) {
        if (s == null) {
            return new String[]{"*"};
        }
        if (s.length == 0) {
            return new String[]{"*"};
        }
        for (int i = 0; i < s.length; i++) {
            if (s[i] == null || s[i].length() == 0) {
                s[i] = "*";
            }
        }
        return s;
    }

    private String formatIndexType() {
        StringBuilder indexes = new StringBuilder();
        if (index != null) {
            for (String s : index) {
                if (s != null && s.length() > 0) {
                    if (indexes.length() > 0) {
                        indexes.append(',');
                    }
                    indexes.append(s);
                }
            }
        }
        if (indexes.length() == 0) {
            indexes.append('*');
        }
        StringBuilder types = new StringBuilder();
        if (type != null) {
            for (String s : type) {
                if (s != null && s.length() > 0) {
                    if (types.length() > 0) {
                        types.append(',');
                    }
                    types.append(s);
                }
            }
        }
        if (types.length() == 0) {
            types.append('*');
        }
        return indexes.append("/").append(types).toString();
    }

}