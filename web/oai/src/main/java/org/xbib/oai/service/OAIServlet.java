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
package org.xbib.oai.service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Date;
import java.util.UUID;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xbib.oai.OAIDateResolution;
import org.xbib.oai.server.OAIServer;
import org.xbib.oai.server.OAIServiceFactory;
import org.xbib.oai.server.identify.IdentifyServerRequest;
import org.xbib.oai.server.identify.IdentifyServerResponse;
import org.xbib.oai.server.listidentifiers.ListIdentifiersServerRequest;
import org.xbib.oai.server.listidentifiers.ListIdentifiersServerResponse;
import org.xbib.oai.server.listmetadataformats.ListMetadataFormatsServerRequest;
import org.xbib.oai.server.listmetadataformats.ListMetadataFormatsServerResponse;
import org.xbib.oai.server.listrecords.ListRecordsServerRequest;
import org.xbib.oai.server.listrecords.ListRecordsServerResponse;
import org.xbib.oai.server.listsets.ListSetsServerRequest;
import org.xbib.oai.server.listsets.ListSetsServerResponse;
import org.xbib.util.DateUtil;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.OAIConstants;
import org.xbib.oai.OAISession;
import org.xbib.oai.util.ResumptionToken;

/**
 *  OAI servlet
 *
 */
public class OAIServlet extends HttpServlet implements OAIConstants {

    private static final Logger logger = LoggerFactory.getLogger(OAIServlet.class.getName());

    private final OAIRequestDumper requestDumper = new OAIRequestDumper();

    private final String responseEncoding = "UTF-8";

    private final String contentType = "text/xml";

    private OAIServer server;

    private OAISession session;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String serviceName = config.getInitParameter("name");
        String serviceURI = config.getInitParameter("uri");
        this.server = serviceName != null ?
                OAIServiceFactory.getService(serviceName) :
                serviceURI != null ?
                        OAIServiceFactory.getService(serviceURI) :
                        OAIServiceFactory.getDefaultService();
    }

    @Override
    public void doGet(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(contentType);
        final OutputStream out = response.getOutputStream();
        logger.info(requestDumper.toString(request));
        if (session == null) {
            session = server.newSession();
        }
        try {
            String verb = request.getParameter(OAIConstants.VERB_PARAMETER);
            Writer writer = new OutputStreamWriter(response.getOutputStream(), responseEncoding);
            switch (verb) {
                case OAIConstants.IDENTIFY: {
                    HttpServerIdentifyRequest oaiRequest = new HttpServerIdentifyRequest(server, session, request);
                    IdentifyServerResponse oaiResponse = new IdentifyServerResponse();
                    server.identify(oaiRequest, oaiResponse);
                    oaiResponse.to(writer);
                    break;
                }
                case OAIConstants.LIST_METADATA_FORMATS: {
                    HttpServerListMetadataFormatsRequest oaiRequest = new HttpServerListMetadataFormatsRequest(server, session, request);
                    ListMetadataFormatsServerResponse oaiResponse = new ListMetadataFormatsServerResponse();
                    server.listMetadataFormats(oaiRequest, oaiResponse);
                    oaiResponse.to(writer);
                    break;
                }
                case OAIConstants.LIST_SETS: {
                    HttpServerListSetsRequest oaiRequest = new HttpServerListSetsRequest(server, session, request);
                    ListSetsServerResponse oaiResponse = new ListSetsServerResponse();
                    server.listSets(oaiRequest, oaiResponse);
                    oaiResponse.to(writer);
                    break;
                }
                case OAIConstants.LIST_IDENTIFIERS: {
                    HttpServerListIdentifiersRequest oaiRequest = new HttpServerListIdentifiersRequest(server, session, request);
                    ListIdentifiersServerResponse oaiResponse = new ListIdentifiersServerResponse();
                    server.listIdentifiers(oaiRequest, oaiResponse);
                    oaiResponse.to(writer);
                    break;
                }
                case OAIConstants.LIST_RECORDS: {
                    HttpServerListRecordsRequest oaiRequest = new HttpServerListRecordsRequest(server, session, request);
                    ListRecordsServerResponse oaiResponse = new ListRecordsServerResponse();
                    server.listRecords(oaiRequest, oaiResponse);
                    oaiResponse.to(writer);
                    break;
                }
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            response.setStatus(500);
        } finally {
            out.flush();
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    private URI getBaseURI(HttpServletRequest request) {
        String uri = request.getRequestURL().toString();
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        if (forwardedHost != null && forwardedHost.length() > 0) {
            uri = uri.replaceAll("://[^/]*", "://" + forwardedHost);
        }
        return URI.create(uri);
    }

    private String getPathInfo(HttpServletRequest request) {
        return request.getPathInfo();
    }

    private class HttpServerIdentifyRequest extends IdentifyServerRequest {

        OAIServer server;
        OAISession session;
        HttpServletRequest request;

        HttpServerIdentifyRequest(OAIServer server, OAISession session, HttpServletRequest request) {
            this.server = server;
            this.session = session;
            this.request = request;
        }

        /*@Override
        public Map<String, List<String>> getParameterMap() {
            Map<String,String[]> m = request.getParameterMap();
            Map<String, List<String>> result = new HashMap();
            for (String key : m.keySet()) {
                result.put(key, Arrays.asList(m.get(key)));
            }
            return result;
        }

        @Override
        public String getPath() {
            return getPathInfo(request);
        }*/
    }

    private class HttpServerListMetadataFormatsRequest extends ListMetadataFormatsServerRequest {

        OAIServer server;
        OAISession session;
        HttpServletRequest request;

        HttpServerListMetadataFormatsRequest(OAIServer server, OAISession session, HttpServletRequest request) {
            this.server = server;
            this.session = session;
            this.request = request;
        }
    }

    private class HttpServerListSetsRequest extends ListSetsServerRequest {

        OAIServer server;
        OAISession session;
        HttpServletRequest request;

        HttpServerListSetsRequest(OAIServer server, OAISession session, HttpServletRequest request) {
            this.server = server;
            this.session = session;
            this.request = request;
        }

        @Override
        public HttpServerListSetsRequest setResumptionToken(ResumptionToken token) {
            return this;
        }

        @Override
        public ResumptionToken getResumptionToken() {
            UUID uuid = UUID.fromString(request.getParameter(OAIConstants.RESUMPTION_TOKEN_PARAMETER));
            return ResumptionToken.get(uuid);
        }

    }

    private class HttpServerListRecordsRequest extends ListRecordsServerRequest {

        OAIServer server;
        OAISession session;
        HttpServletRequest request;

        HttpServerListRecordsRequest(OAIServer server, OAISession session, HttpServletRequest request) {
            this.server = server;
            this.session = session;
            this.request = request;
        }

        @Override
        public HttpServerListRecordsRequest setFrom(Date from, OAIDateResolution oaiDateResolution) {
            return this;
        }

        @Override
        public Date getFrom() {
            return DateUtil.parseDateISO(request.getParameter(OAIConstants.FROM_PARAMETER));
        }

        @Override
        public HttpServerListRecordsRequest setUntil(Date until, OAIDateResolution oaiDateResolution) {
            return this;
        }

        @Override
        public Date getUntil() {
            return DateUtil.parseDateISO(request.getParameter(OAIConstants.UNTIL_PARAMETER));
        }

        @Override
        public String getSet() {
            return request.getParameter(OAIConstants.SET_PARAMETER);
        }

        @Override
        public String getMetadataPrefix() {
            return request.getParameter(OAIConstants.METADATA_PREFIX_PARAMETER);
        }

        @Override
        public ListRecordsServerRequest setResumptionToken(ResumptionToken token) {
            return this;
        }

        @Override
        public ResumptionToken getResumptionToken() {
            return ResumptionToken.get(UUID.fromString(request.getParameter(OAIConstants.RESUMPTION_TOKEN_PARAMETER)));
        }


        /*public Map<String, List<String>> getParameterMap() {
            Map<String,String[]> m = request.getParameterMap();
            Map<String, List<String>> result = new HashMap();
            for (String key : m.keySet()) {
                result.put(key, Arrays.asList(m.get(key)));
            }
            return result;
        }*/

        public String getPath() {
            return getPathInfo(request);
        }
    }

    private class HttpServerListIdentifiersRequest extends ListIdentifiersServerRequest {

        OAIServer server;
        OAISession session;
        HttpServletRequest request;

        HttpServerListIdentifiersRequest(OAIServer server, OAISession session, HttpServletRequest request) {
            this.server = server;
            this.session = session;
            this.request = request;
        }

        @Override
        public HttpServerListIdentifiersRequest setFrom(Date from, OAIDateResolution oaiDateResolution) {
            return this;
        }

        @Override
        public Date getFrom() {
            return DateUtil.parseDateISO(request.getParameter(OAIConstants.FROM_PARAMETER));
        }

        @Override
        public HttpServerListIdentifiersRequest setUntil(Date until, OAIDateResolution oaiDateResolution) {
            return this;
        }

        @Override
        public Date getUntil() {
            return DateUtil.parseDateISO(request.getParameter(OAIConstants.UNTIL_PARAMETER));
        }

        @Override
        public String getSet() {
            return request.getParameter(OAIConstants.SET_PARAMETER);
        }

        @Override
        public String getMetadataPrefix() {
            return request.getParameter(OAIConstants.METADATA_PREFIX_PARAMETER);
        }

        @Override
        public HttpServerListIdentifiersRequest setResumptionToken(ResumptionToken token) {
            return this;
        }

        @Override
        public ResumptionToken getResumptionToken() {
            UUID uuid = UUID.fromString(request.getParameter(OAIConstants.RESUMPTION_TOKEN_PARAMETER));
            return ResumptionToken.get(uuid);
        }

    }
}
