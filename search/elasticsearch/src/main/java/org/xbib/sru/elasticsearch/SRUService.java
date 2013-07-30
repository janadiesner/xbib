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
package org.xbib.sru.elasticsearch;

import java.io.IOException;
import java.net.URI;
import java.util.ResourceBundle;

import org.xbib.elasticsearch.support.CQLSearchSupport;
import org.xbib.sru.SRUConstants;
import org.xbib.sru.service.SRUService;

/**
 * Elasticseach SRU service
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class SRUService implements org.xbib.sru.service.SRUService {

    private static final ResourceBundle bundle = ResourceBundle.getBundle("org.xbib.sru.elasticsearch");

    private final String recordPacking = bundle.getString(SRUConstants.RECORDPACKING_PROPERTY);

    private final String recordSchema = bundle.getString(SRUConstants.RECORDSCHEMA_PROPERTY);

    private final String version = bundle.getString(SRUConstants.VERSION_PROPERTY);

    private final CQLSearchSupport support = new CQLSearchSupport();

    @Override
    public URI getURI() {
        return URI.create(bundle.getString("uri"));
    }

    @Override
    public void close(org.xbib.sru.client.SRUClient client) throws IOException {
        client.shutdown();
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getRecordPacking() {
        return recordPacking;
    }

    @Override
    public String getRecordSchema() {
        return recordSchema;
    }

    @Override
    public String getEncoding() {
        return "UTF-8"; // always UTF-8
    }

    public org.xbib.sru.client.SRUClient newClient() {
        return new SRUClient(this, support.newClient());
    }

    public org.xbib.sru.client.SRUClient newClient(URI uri) {
        return new SRUClient(this, support.newClient(uri));
    }
}