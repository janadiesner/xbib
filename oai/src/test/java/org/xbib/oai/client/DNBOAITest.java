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
package org.xbib.oai.client;

import java.io.IOException;
import java.io.StringWriter;

import org.testng.annotations.Test;
import org.xbib.oai.OAIDateResolution;
import org.xbib.oai.client.listrecords.ListRecordsListener;
import org.xbib.util.DateUtil;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.client.listrecords.ListRecordsRequest;

public class DNBOAITest {

    private final Logger logger = LoggerFactory.getLogger(DNBOAITest.class.getName());

    @Test
    public void testDNBOAI() throws Exception {

        OAIClient client = OAIClientFactory.newClient("DNB");
        ListRecordsRequest request = client.newListRecordsRequest()
                .setMetadataPrefix("RDFxml")
                .setSet("authorities")
                .setFrom(DateUtil.parseDateISO("2012-01-23T00:00:00Z"), OAIDateResolution.SECOND)
                .setUntil(DateUtil.parseDateISO("2012-01-23T01:00:00Z"), OAIDateResolution.SECOND);
        do {
            //RdfXmlReader rdfXmlReader = new RdfXmlReader();
            /*final TripleListener stmt = new TripleListener() {

                @Override
                public TripleListener begin() {
                    return this;
                }

                @Override
                public TripleListener startPrefixMapping(String prefix, String uri) {
                    return this;
                }

                @Override
                public TripleListener endPrefixMapping(String prefix) {
                    return this;
                }

                @Override
                public TripleListener newIdentifier(IRI uri) {
                    getResource().id(uri);
                    return this;
                }

                @Override
                public TripleListener triple(Triple statement) {
                    getResource().add(statement);
                    return this;
                }

                @Override
                public TripleListener end() {
                    return this;
                }
            };*/
            //reader.setTripleListener(stmt);
            //final XmlHandler handler = reader.getHandler();
            //ResourceContext<Resource> resourceContext = new ResourceContext<Resource>();

            //RdfXmlReader rdfXmlReader = new RdfXmlReader();

            /*MetadataHandler metadataHandler = new MetadataHandler() {

                @Override
                public void startDocument() throws SAXException {
                    //handler.startDocument();
                    //setResource(new SimpleResource());
                }

                @Override
                public void endDocument() throws SAXException {
                    //handler.endDocument();
                    try {
                        StringWriter sw = new StringWriter();
                        new TurtleWriter(sw)
                                .write(getResource());
                        logger.info(sw.toString());
                    } catch (IOException ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }

                @Override
                public void startPrefixMapping(String string, String string1) throws SAXException {
                    handler.startPrefixMapping(string, string1);
                }

                @Override
                public void endPrefixMapping(String string) throws SAXException {
                    handler.endPrefixMapping(string);
                }

                @Override
                public void startElement(String ns, String localname, String string2, Attributes atrbts) throws SAXException {
                    handler.startElement(ns, localname, string2, atrbts);
                }

                @Override
                public void endElement(String ns, String localname, String string2) throws SAXException {
                    handler.endElement(ns, localname, string2);
                }

                @Override
                public void characters(char[] chars, int i, int i1) throws SAXException {
                    handler.characters(chars, i, i1);
                }
            };*/
            StringWriter sw = new StringWriter();
            //request.addHandler(metadataHandler);
            ListRecordsListener listener = new ListRecordsListener(request);
            try {
                request.prepare().execute(listener).waitFor();
                if (listener.getResponse() != null) {
                    listener.getResponse().to(sw);
                    logger.info("response = {}", sw);
                }
                request = client.resume(request, listener.getResumptionToken());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                request = null;
            }

        } while (request != null);
        client.close();
    }

}
