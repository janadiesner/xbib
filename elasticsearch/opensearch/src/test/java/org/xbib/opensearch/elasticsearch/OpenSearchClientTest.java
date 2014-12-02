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

import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.provider.managed.FeedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

/**
 * BulkIndexerTest CQL Feed controller
 *
 */
public class OpenSearchClientTest {

    /** the logger */
    private static final Logger logger = LogManager.getLogger(OpenSearchClientTest.class.getName());

    @Test
    public void testFeedControllerCQL() throws Exception {
        Properties p = new Properties();
        p.put(FeedConfiguration.PROP_SUB_URI_NAME,"test");
        p.put(FeedConfiguration.PROP_NAME_ADAPTER_CLASS,"org.xbib.atom.ElasticsearchAbderaAdapter");
        p.put(FeedConfiguration.PROP_FEED_CONFIG_LOCATION_NAME, "");
        //p.put(OpenSearchAtomFeedFactory.FEED_URI_PROPERTY_KEY,"sniff://hostname:9300");
        p.put(OpenSearchAtomFeedFactory.FEED_CLUSTER_PROPERTY_KEY,"elasticsearch");
        p.put(OpenSearchAtomFeedFactory.FEED_HOST_PROPERTY_KEY,"hostname");
        p.put(OpenSearchAtomFeedFactory.FEED_PORT_PROPERTY_KEY, 9300);
        p.put("feed.stylesheet","xsl/es-mods-atom.xsl");
        p.put("feed.author","Jörg Prante");
        p.put("feed.title.pattern","Ihre Suche war : {0}");
        p.put("feed.subtitle.pattern","{0} Treffer in {1} Sekunden");
        p.put("feed.constructiontime.pattern","Feed erzeugt in {0,number} Millisekunden");
        OpenSearchAtomFeedFactory controller = new OpenSearchAtomFeedFactory();
        try {
            Feed feed = controller.createFeed(p, "dc.title = test", 0, 10);
            StringWriter sw = new StringWriter();
            feed.writeTo("prettyxml", sw);
            logger.info(sw.toString());
        } catch (NoNodeAvailableException e) {
            logger.warn(e.getMessage());
        } catch (IOException e) {
            logger.warn(e.getMessage());
        }
    }
}
