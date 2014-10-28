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

import org.testng.annotations.Test;
import org.xbib.oai.OAIDateResolution;
import org.xbib.oai.client.listrecords.ListRecordsListener;
import org.xbib.rdf.memory.MemoryLiteral;
import org.xbib.util.DateUtil;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.client.listrecords.ListRecordsRequest;
import org.xbib.oai.rdf.RdfMetadataHandler;
import org.xbib.oai.rdf.RdfResourceHandler;
import org.xbib.rdf.context.ResourceContext;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.StringWriter;

/**
 * DOAJ client test
 *
 */
public class DOAJClientTest {

    private final Logger logger = LoggerFactory.getLogger(DOAJClientTest.class.getName());

    @Test
    public void testListRecordsDOAJ() throws Exception {

        final RdfMetadataHandler metadataHandler = new RdfMetadataHandler();
        final RdfResourceHandler resourceHandler = new DOAJResourceHandler(metadataHandler.getResourceContext());
        //final RdfOutput out = new MyOutput(metadataHandler.getContext());

        metadataHandler.setHandler(resourceHandler);
            //.setOutput(out);

        OAIClient client = OAIClientFactory.newClient("DOAJ");
        ListRecordsRequest request = client.newListRecordsRequest()
                .setFrom( DateUtil.parseDateISO("2014-04-16T00:00:00Z"), OAIDateResolution.DAY)
                .setUntil(DateUtil.parseDateISO("2014-04-17T00:00:00Z"), OAIDateResolution.DAY)
                .setMetadataPrefix("oai_dc");

        do {
            try {
                ListRecordsListener listener = new ListRecordsListener(request);
                request.addHandler(metadataHandler);
                request.prepare().execute(listener).waitFor();
                if (listener.getResponse() != null) {
                    StringWriter sw = new StringWriter();
                    listener.getResponse().to(sw);
                    logger.info("response  = {}", sw);
                }
                request = client.resume(request, listener.getResumptionToken());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                request= null;
            }
        } while (request != null);

        client.close();
    }

    private final IRI ISSN = IRI.create("urn:ISSN");
    private final IRI EISSN = IRI.create("urn:EISSN");
    private final IRI LCCN = IRI.create("urn:LCC");

    class DOAJResourceHandler extends RdfResourceHandler {

        public DOAJResourceHandler(ResourceContext context) {
            super(context);
        }

        @Override
        public Object toObject(QName name, String content) {
            String s = name.getLocalPart();
            if (s.equals("identifier")) {
                if (content.startsWith("http://")) {
                    return new MemoryLiteral(content).type(IRI.create("xsd:anyUri"));
                }
                if (content.startsWith("issn: ")) {
                    return new MemoryLiteral(content.substring(6)).type(ISSN);
                }
                if (content.startsWith("eissn: ")) {
                    return new MemoryLiteral(content.substring(7)).type(EISSN);
                }
            } else if (s.equals("subject")) {
                if (content.startsWith("LCC: ")) {
                    return new MemoryLiteral(content.substring(5)).type(LCCN);
                }
            }
            return content;
        }
    }

    /*class MyOutput extends RdfOutput {

        TurtleWriter writer;

        MyOutput(IRINamespaceContext context) throws IOException{
            this.writer = new TurtleWriter()
                    .setContext(context)
                    .writeNamespaces();
        }

        @Override
        public RdfOutput output(ResourceContext resourceContext) throws IOException {
            StringWriter sw = new StringWriter();
            writer.output(sw);
            writer.write(resourceContext.getResource());
            logger.info("out = {}", sw);
            return this;
        }
    }*/

}
