package org.xbib.rdf.io.xml;

import org.testng.annotations.Test;
import org.xbib.helper.StreamTester;
import org.xbib.iri.IRI;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.memory.MemoryResourceContext;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.text.CharUtils;
import org.xbib.text.UrlEncoding;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

public class OAITest extends StreamTester {

    @Test
    public void testOAIListRecords() throws Exception {
        String filename = "/org/xbib/rdf/io/xml/oai-listrecords.xml";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }

        IRINamespaceContext context = IRINamespaceContext.newInstance();
        context.addNamespace("oaidc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        context.addNamespace("dc", "http://purl.org/dc/elements/1.1/");

        final MemoryResourceContext resourceContext = new MemoryResourceContext();
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
                    String s = UrlEncoding.encode(value, CharUtils.Profile.SCHEMESPECIFICPART.filter());
                    resourceContext.getResource().id(IRI.create("id:" + s));
                }
            }

            @Override
            public boolean skip(QName name) {
                return name.getLocalPart().startsWith("@");
            }

        };
        StringWriter sw = new StringWriter();
        //FileWriter sw = new FileWriter("oai.ttl");
        TurtleWriter writer = new TurtleWriter(sw);
        writer.setNamespaceContext(context);
        writer.writeNamespaces();
        xmlHandler.setBuilder(writer)
            .setDefaultNamespace("oai", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        new XmlParser()
                .setHandler(xmlHandler)
                .parse(new InputStreamReader(in, "UTF-8"), writer);
        writer.close();
        sw.close();
        assertStream(getClass().getResource("oai.ttl").openStream(),
                new ByteArrayInputStream(sw.toString().getBytes()));
    }
}
