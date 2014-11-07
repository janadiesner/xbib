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
package org.xbib.rdf.io.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.LinkedList;
import javax.xml.namespace.QName;
import org.testng.annotations.Test;
import org.xbib.helper.StreamTester;
import org.xbib.iri.IRI;
import org.xbib.rdf.Triple;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.memory.MemoryContext;
import org.xbib.rdf.memory.MemoryResource;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.text.CharUtils.Profile;
import org.xbib.text.UrlEncoding;

import static org.testng.Assert.assertEquals;

public class XmlReaderTest extends StreamTester {

    @Test
    public void testOAIDC() throws Exception {
        String filename = "/org/xbib/rdf/io/xml/oro-eprint-25656.xml";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }

        IRINamespaceContext context = IRINamespaceContext.newInstance();
        context.addNamespace("oaidc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        context.addNamespace("dc", "http://purl.org/dc/elements/1.1/");

        final MemoryContext resourceContext = new MemoryContext();
        resourceContext.setNamespaceContext(context);

        XmlHandler xmlHandler = new AbstractXmlResourceHandler(resourceContext) {

            @Override
            public boolean isResourceDelimiter(QName name) {
                return "oai_dc".equals(name.getLocalPart());
            }

            @Override
            public void identify(QName name, String value, IRI identifier) {
                if ("identifier".equals(name.getLocalPart()) && identifier == null) {
                    // make sure we can build an opaque IRI, whatever is out there
                    String s = UrlEncoding.encode(value, Profile.SCHEMESPECIFICPART.filter());
                    resourceContext.getResource().id(IRI.create("id:" + s));
                }
            }
            
            @Override
            public boolean skip(QName name) {
                // skip dc:dc element
                return "dc".equals(name.getLocalPart());
            }

        };
        StringWriter sw = new StringWriter();
        TurtleWriter writer = new TurtleWriter(sw);
        writer.setNamespaceContext(context);
        writer.writeNamespaces();
        xmlHandler.setBuilder(writer);
        new XmlParser()
                .setHandler(xmlHandler)
                .parse(new InputStreamReader(in, "UTF-8"), writer);
        writer.close();
        sw.close();

        assertStream(getClass().getResource("dc.ttl").openStream(),
                new ByteArrayInputStream(sw.toString().getBytes()));
    }

    @Test
    public void testXmlArray() throws Exception {
        String filename = "/org/xbib/rdf/io/xml/array.xml";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }
        IRINamespaceContext context = IRINamespaceContext.newInstance();
        final MemoryContext resourceContext = new MemoryContext();
        resourceContext.setNamespaceContext(context);
        AbstractXmlHandler xmlHandler = new AbstractXmlResourceHandler(resourceContext) {

            @Override
            public boolean isResourceDelimiter(QName name) {
                return false;
            }

            @Override
            public void identify(QName name, String value, IRI identifier) {
                if (identifier == null) {
                    resourceContext.getResource().id(IRI.create("id:1"));
                }
            }

            @Override
            public boolean skip(QName name) {
                return false;
            }

        };
        final LinkedList<Triple> triples = new LinkedList<Triple>();
        xmlHandler.setDefaultNamespace("xml", "http://xmltest")
                .setBuilder(new Triple.Builder() {
                    @Override
                    public Triple.Builder begin() {
                        return this;
                    }

                    @Override
                    public Triple.Builder startPrefixMapping(String prefix, String uri) {
                        return this;
                    }

                    @Override
                    public Triple.Builder endPrefixMapping(String prefix) {
                        return this;
                    }

                    @Override
                    public Triple.Builder newIdentifier(IRI identifier) {
                        return this;
                    }

                    @Override
                    public Triple.Builder triple(Triple triple) {
                        triples.add(triple);
                        return this;
                    }

                    @Override
                    public Triple.Builder end() {
                        return this;
                    }
                });
        MemoryResource.reset();
        new XmlParser()
                .setHandler(xmlHandler)
                .parse(new InputStreamReader(in, "UTF-8"), null);
        assertEquals(triples.toString(),
                "[id:1 xml:dates _:b1, _:b1 xml:date 2001, _:b1 xml:date 2002, _:b1 xml:date 2003]"
        );
    }

    @Test
    public void testXmlAttribute() throws Exception {
        String filename = "/org/xbib/rdf/io/xml/attribute.xml";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }
        IRINamespaceContext context = IRINamespaceContext.newInstance();
        final MemoryContext resourceContext = new MemoryContext();
        resourceContext.setNamespaceContext(context);
        AbstractXmlResourceHandler xmlHandler = new AbstractXmlResourceHandler(resourceContext) {

            @Override
            public boolean isResourceDelimiter(QName name) {
                return false;
            }

            @Override
            public void identify(QName name, String value, IRI identifier) {
                if (identifier ==null) {
                    resourceContext.getResource().id(IRI.create("id:1"));
                }
            }

            @Override
            public boolean skip(QName name) {
                return false;
            }

        };
        LinkedList<Triple> triples = new LinkedList<>();
        xmlHandler.setDefaultNamespace("xml", "http://localhost")
                .setBuilder(new Triple.Builder() {
                    @Override
                    public Triple.Builder begin() {
                        return this;
                    }

                    @Override
                    public Triple.Builder startPrefixMapping(String prefix, String uri) {
                        return this;
                    }

                    @Override
                    public Triple.Builder endPrefixMapping(String prefix) {
                        return this;
                    }

                    @Override
                    public Triple.Builder newIdentifier(IRI identifier) {
                        return this;
                    }

                    @Override
                    public Triple.Builder triple(Triple triple) {
                        triples.add(triple);
                        return this;
                    }

                    @Override
                    public Triple.Builder end() {
                        return this;
                    }
                });
        MemoryResource.reset();
        new XmlParser()
                .setHandler(xmlHandler)
                .parse(new InputStreamReader(in, "UTF-8"), null);
        assertEquals( triples.toString(),
                "[id:1 xml:dates _:b1, _:b1 xml:date _:b2, _:b2 xml:@href 1, _:b1 xml:date _:b4, _:b4 xml:@href 2, _:b1 xml:date _:b6, _:b6 xml:@href 3, _:b1 xml:date _:b8, _:b8 xml:hello World]");

    }

}
