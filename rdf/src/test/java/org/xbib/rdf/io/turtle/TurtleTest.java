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
package org.xbib.rdf.io.turtle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Resource;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.memory.MemoryResourceContext;

public class TurtleTest<R extends Resource> extends Assert {

    private final Logger logger = LoggerFactory.getLogger(TurtleTest.class.getName());

    @Test
    public void testTurtleGND() throws Exception {
        IRINamespaceContext context = IRINamespaceContext.newInstance();
        context.addNamespace("gnd", "http://d-nb.info/gnd/");
        InputStream in = getClass().getResourceAsStream("GND.ttl");
        TurtleParser reader = new TurtleParser().setBaseIRI(IRI.create("http://d-nb.info/gnd/"))
                .context(context);
        reader.parse(new InputStreamReader(in, "UTF-8"), null);
    }

    @Test
    public void testTurtleReader() throws Exception {
        StringBuilder sb = new StringBuilder();
        String filename = "turtle-demo.ttl";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        String s1 = sb.toString().trim();
        ResourceContext<R> resourceContext = createResourceContext();

        IRINamespaceContext context = IRINamespaceContext.newInstance();
        context.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
        context.addNamespace("dcterms", "http://purl.org/dc/terms/");

        StringWriter sw = new StringWriter();
        TurtleWriter writer = new TurtleWriter(sw);
        writer.setNamespaceContext(context);
        writer.writeNamespaces();
        writer.write(resourceContext);
        String s2 = sw.toString().trim();
        assertEquals(s2, s1);
    }

    private ResourceContext<R> createResourceContext() {
        MemoryResourceContext<R> context = new MemoryResourceContext();
        R resource = context.newResource();
        resource.id(IRI.create("urn:doc1"));
        resource.add("dc:creator", "Smith");
        resource.add("dc:creator", "Jones");
        Resource r = resource.newResource("dcterms:hasPart")
                .add("dc:title", "This is a part")
                .add("dc:title", "of the sample title")
                .add("dc:creator", "Jörg Prante")
                .add("dc:date", "2009");
        resource.add("dc:title", "A sample title");
        r = resource.newResource("dcterms:isPartOf")
                .add("dc:title", "another")
                .add("dc:title", "title");
        return context;
    }

    @Test
    public void testTurtleWrite() throws Exception {
        ResourceContext<R> resourceContext = createResourceContext2();
        StringWriter sw = new StringWriter();
        TurtleWriter writer = new TurtleWriter(sw);
        writer.writeNamespaces();
        writer.write(resourceContext);
        logger.info(sw.toString().trim());
    }

    private ResourceContext<R> createResourceContext2() {
        MemoryResourceContext<R> context = new MemoryResourceContext();
        R r = context.newResource();
        r.id(IRI.create("urn:res"))
                .add("dc:title", "Hello")
                .add("dc:title", "World")
                .add("xbib:person", "Jörg Prante")
                .add("dc:subject", "An")
                .add("dc:subject", "example")
                .add("dc:subject", "for")
                .add("dc:subject", "a")
                .add("dc:subject", "sequence")
                .add("http://purl.org/dc/terms/place", "Köln");
        // sequence optimized for turtle output
        Resource r1 = r.newResource("urn:res1")
                .add("property1", "value1")
                .add("property2", "value2");
        Resource r2 = r.newResource("urn:res2")
                .add("property3", "value3")
                .add("property4", "value4");
        Resource r3 = r.newResource("urn:res3")
                .add("property5", "value5")
                .add("property6", "value6");
        return context;
    }

    @Test
    public void testTurtleResourceIndent() throws Exception {
        ResourceContext<R> resourceContext = createNestedResources();
        StringWriter sw = new StringWriter();
        TurtleWriter writer = new TurtleWriter(sw);
        writer.writeNamespaces();
        writer.write(resourceContext);
        logger.info(sw.toString().trim());
    }

    private ResourceContext<R> createNestedResources() {
        MemoryResourceContext<R> context = new MemoryResourceContext();
        R r = context.newResource();
        r.id(IRI.create("urn:res"))
                .add("dc:title", "Hello")
                .add("dc:title", "World")
                .add("xbib:person", "Jörg Prante")
                .add("dc:subject", "An")
                .add("dc:subject", "example")
                .add("dc:subject", "for")
                .add("dc:subject", "a")
                .add("dc:subject", "sequence")
                .add("http://purl.org/dc/terms/place", "Köln");
        // sequence optimized for turtle output
        Resource r1 = r.newResource("urn:res1")
                .add("property1", "value1")
                .add("property2", "value2");
        Resource r2 = r1.newResource("urn:res2")
                .add("property3", "value3")
                .add("property4", "value4");
        Resource r3 = r.newResource("urn:res3")
                .add("property5", "value5")
                .add("property6", "value6");
        return context;
    }


}
