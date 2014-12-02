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
package org.xbib.opensearch.elasticsearch;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.provider.managed.FeedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.atom.AbderaFeedBuilder;
import org.xbib.atom.AtomFeedFactory;
import org.xbib.atom.AtomFeedProperties;
import org.xbib.common.settings.ImmutableSettings;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.search.CQLRequest;
import org.xbib.elasticsearch.search.CQLResponse;
import org.xbib.elasticsearch.search.SearchSupport;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * OpenSearch Atom feed controller for Elasticsearch. The results are wrapped up in an Atom
 * feed format.
 *
 */
public class OpenSearchAtomFeedFactory implements AtomFeedFactory {

    public final static String FEED_BASE_URI_KEY = "feed.base.uri";
    public final static String FEED_CLUSTER_PROPERTY_KEY = "feed.cluster";
    public final static String FEED_HOST_PROPERTY_KEY = "feed.host";
    public final static String FEED_PORT_PROPERTY_KEY = "feed.port";
    public final static String FEED_AUTHOR_PROPERTY_KEY = "feed.author";
    public final static String FEED_TITLE_PATTERN_PROPERTY_KEY = "feed.title.pattern";
    public final static String FEED_SUBTITLE_PATTERN_PROPERTY_KEY = "feed.subtitle.pattern";
    public final static String FEED_RESULTSETLIMIT_FROM = "feed.resultset.limit.from";
    public final static String FEED_RESULTSETLIMIT_SIZE = "feed.resultset.limit.size";
    public final static String FEED_CONSTRUCTION_TIME_PATTERN_KEY = "feed.constructiontime.pattern";
    public final static String FEED_STYLESHEET_PROPERTY_KEY = "feed.stylesheet";
    public final static String FEED_SERVICE_PATH_KEY = "feed.service.path";
    public final static String FEED_INDEX = "feed.index";
    public final static String FEED_TYPE = "feed.type";
    
    private final static Logger logger = LogManager.getLogger(OpenSearchAtomFeedFactory.class.getName());
    protected AbderaFeedBuilder builder;
    private SearchSupport support = new SearchSupport();

    public OpenSearchAtomFeedFactory() {
    }

    public Feed createFeed(Properties properties, String query, int from, int size)
            throws IOException {
        return createFeed(Abdera.getInstance(),
                "",
                "",
                "",
                AtomFeedProperties.getFeedConfiguration(query, properties, null),
                query, from, size);
    }

    /**
     * Create Atom feed
     *
     * @param config the feed configuration
     * @param query the query
     * @throws java.io.IOException
     */
    @Override
    public Feed createFeed(RequestContext request, FeedConfiguration config,
            String query, int from, int size) throws IOException {
        if (config == null) {
            throw new IOException("feed configuration must not be null");
        }
        return createFeed(request.getAbdera(),
                request.getBaseUri().toASCIIString(),
                request.getContextPath(),
                config.getHref(request),
                config,
                query, from, size);
    }

    /**
     * Create Atom feed
     *
     * @param abdera the Abdera instance
     * @param query the query
     * @return the Atom feed or null
     * @throws java.io.IOException if Atom feed can not be created
     */
    public Feed createFeed(Abdera abdera,
            String baseURI, String contextPath, String servicePath,
            FeedConfiguration config,
            String query, int from, int size) throws IOException {

        AtomFeedProperties properties = new AtomFeedProperties(config);
        properties.setAbdera(abdera);
        properties.setBaseURI(baseURI);
        properties.setContextPath(contextPath);
        properties.setServicePath(servicePath);
        properties.setFrom(from);
        properties.setSize(size);
        return createFeed(properties, query);
    }

    public Feed createFeed(AtomFeedProperties config,
            String query) throws IOException {
        // load properties from feed config
        String feedConfigLocation = config.getFeedConfigLocation();
        Properties properties = new Properties();
        if (feedConfigLocation != null) {
            InputStream in = getClass().getResourceAsStream(feedConfigLocation);
            if (in != null) {
                properties.load(in);
            } else {
                throw new IOException("feed config not found: " + feedConfigLocation);
            }
        }
        // transfer our properties to AtomFeedProperties
        config.setTitlePattern(properties.getProperty(FEED_TITLE_PATTERN_PROPERTY_KEY));
        config.setSubtitlePattern(properties.getProperty(FEED_SUBTITLE_PATTERN_PROPERTY_KEY));
        config.setTimePattern(properties.getProperty(FEED_CONSTRUCTION_TIME_PATTERN_KEY));
        config.setStylesheet(properties.getProperty(FEED_STYLESHEET_PROPERTY_KEY));
        if (properties.containsKey(FEED_SERVICE_PATH_KEY)) {
            config.setServicePath(properties.getProperty(FEED_SERVICE_PATH_KEY));
        }
        //String uriStr = properties.getProperty(FEED_URI_PROPERTY_KEY, "es://localhost:9300?es.cluster.name=joerg");
        //URI uri = URI.create(uriStr);
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", properties.containsKey(FEED_CLUSTER_PROPERTY_KEY) ?
                        properties.getProperty(FEED_CLUSTER_PROPERTY_KEY) : "elasticsearch")
                .put("host", properties.containsKey(FEED_HOST_PROPERTY_KEY) ?
                        properties.getProperty(FEED_HOST_PROPERTY_KEY) : "localhost")
                .put("port", properties.containsKey(FEED_PORT_PROPERTY_KEY) ?
                        properties.getProperty(FEED_PORT_PROPERTY_KEY) : "9300")
                .build();
        support.newClient(settings.getAsMap());
        String index = properties.getProperty(FEED_INDEX);
        String type =  properties.getProperty(FEED_TYPE);
        try {
            long t0 = System.currentTimeMillis();
            this.builder = new AbderaFeedBuilder(config, query);
            Logger logger = LogManager.getLogger(OpenSearchAtomFeedFactory.class);
            CQLRequest request = support.newSearchRequest();
            CQLResponse response =request.index(index)
                    .type(type)
                    .from(config.getFrom())
                    .size(config.getSize())
                    .cql(query)
                    .execute(logger);
            response.to(builder);
            long t1 = System.currentTimeMillis();
            return builder.getFeed(query, t1 - t0,
                    -1L, config.getFrom(), config.getSize());
        } catch (Exception e) {
            logger.error("atom feed query " + query + " session is unresponsive", e);
            throw new IOException(e.getMessage());
        } finally {
            logger.info("atom feed query completed: {}", query);
        }
    }
}
