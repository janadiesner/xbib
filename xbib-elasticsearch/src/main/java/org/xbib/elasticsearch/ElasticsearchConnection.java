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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.xbib.io.Connection;
import org.xbib.io.util.URIUtil;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

/**
 *  Elasticsearch connection
 *
 */
public class ElasticsearchConnection<S extends ElasticsearchSession>
    implements Connection<S> {
    
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchConnection.class.getName());
    private final static String DEFAULT_CLUSTER_NAME = "elasticsearch";
    private final static URI DEFAULT_URI = URI.create("es://interfaces:9300");
    private final static Map<URI, ElasticsearchConnection> instances = new HashMap();
    private final URI uri;
    private final String clusterName;
    private boolean ipv6 = false;
    private boolean sniff = false;

    private ElasticsearchConnection() {
        this(findURI());
    }

    private ElasticsearchConnection(URI uri) {
        this.uri = uri;
        this.clusterName = findClusterName(uri);
    }    
    
    public static ElasticsearchConnection getInstance() {
        return getInstance(findURI());
    }

    public static ElasticsearchConnection getInstance(URI uri) {
        if (!instances.containsKey(uri)) {
            instances.put(uri, new ElasticsearchConnection(uri));
        }
        return instances.get(uri);
    }    
    
    @Override
    public ElasticsearchConnection setURI(URI uri) {
        return this;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public S createSession() throws IOException {
        ElasticsearchSession session = new ElasticsearchSession(this, null);        
        return (S)session;
    }

    public S createSession(Logger logger) throws IOException {
        ElasticsearchSession session = new ElasticsearchSession(this, logger);
        return (S)session;
    }
    
    @Override
    public void close() throws IOException {
        instances.clear();
        // nothing to close
    }
    
    public String getClusterName() {
        return clusterName;
    }    
    
    public boolean isIPV6() {
        return ipv6;
    }
    
    public boolean isSniff() {
        return sniff;
    }
    
    private static URI findURI() {
        URI uri = DEFAULT_URI;
        String hostname = "localhost";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
            logger.debug("the hostname is {}", hostname);
            Properties p = new Properties();
            try (InputStream in = ElasticsearchSession.class.getResource("/org/xbib/elasticsearch/cluster.properties").openStream()) {
                p.load(in);
                if (p.containsKey(hostname)) {
                    uri = URI.create(p.getProperty(hostname));
                    logger.debug("URI found in cluster.properties for hostname {} = {}",
                        hostname, uri);
                    return uri;
                }
            }
        } catch (UnknownHostException e) {
            logger.warn("can't resolve host name, probably something wrong with network config: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        logger.debug("URI for hostname {} = {}", hostname, uri);
        return uri;
    }
    
    private String findClusterName(URI uri) {
        String clustername;
        try {
            // look for URI parameters
            Map<String,String> map = URIUtil.parseQueryString(uri);
            ipv6 = "true".equalsIgnoreCase(map.get("ipv6"));
            sniff = "true".equalsIgnoreCase(map.get("sniff"));
            clustername = map.get("es.cluster.name");
            if (clustername != null) {
                logger.info("cluster name found in URI {}", uri);
                return clustername;
            }
        } catch (UnsupportedEncodingException ex) {
            logger.warn(ex.getMessage(), ex);
        }
        logger.info("cluster name not found in URI {}, parameter es.cluster.name", uri);
        clustername = System.getProperty("es.cluster.name");
        if (clustername != null) {
            logger.info("cluster name found in es.cluster.name system property = {}", clustername);
            return clustername;
        }
        logger.info("cluster name not found, falling back to default " + DEFAULT_CLUSTER_NAME);
        clustername = DEFAULT_CLUSTER_NAME;
        return clustername;
    }

}
