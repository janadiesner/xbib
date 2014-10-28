package org.xbib.rdf.xcontent;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.Loggers;
import org.xbib.rdf.Identifiable;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.content.DefaultContentBuilder;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.memory.MemoryLiteral;
import org.xbib.rdf.memory.MemoryResource;

public class DefaultContentBuilderTest<S extends Identifiable, P extends Property, O extends Node>
    extends Assert {

    private final Logger logger = Loggers.getLogger(DefaultContentBuilderTest.class);

    @Test
    public void testContentBuilder() throws Exception {
        Resource<S, P, O> resource = new MemoryResource();
        MemoryLiteral<String> l = new MemoryLiteral<String>()
                .object("2013")
                .type(IRI.create("xsd:gYear"));
        resource.id(IRI.create("urn:res"))
                .add("urn:property", "Hello World")
                .add("urn:date", l)
                .add("urn:link", IRI.create("urn:pointer"));
        ResourceContext context = resource.context();
        DefaultContentBuilder defaultContentBuilder = new DefaultContentBuilder();
        String result = defaultContentBuilder.build(context, resource);
        assertEquals("{\"urn:property\":\"Hello World\",\"urn:date\":2013,\"urn:link\":\"urn:pointer\"}", result);
    }

    @Test
    public void testContentBuilderSingleEmbedded() throws Exception {
        Resource<S, P, O> resource = new MemoryResource();
        MemoryLiteral<String> l = new MemoryLiteral<String>()
                .object("2013")
                .type(IRI.create("xsd:gYear"));
        resource.id(IRI.create("urn:res"))
                .add("urn:property", "Hello World")
                .add("urn:date", l)
                .add("rdf:type", IRI.create("urn:type1"))
                .newResource("urn:embedded")
                .add("rdf:type", IRI.create("urn:type2"));
        ResourceContext context = resource.context();
        DefaultContentBuilder defaultContentBuilder = new DefaultContentBuilder();
        String result = defaultContentBuilder.build(context, resource);
        assertEquals(result, "{\"urn:property\":\"Hello World\",\"urn:date\":2013,\"rdf:type\":\"urn:type1\",\"urn:embedded\":{\"rdf:type\":\"urn:type2\"}}");
    }

    @Test
    public void testContentBuilderDoubleEmbedded() throws Exception {
        Resource<S, P, O> resource = new MemoryResource();
        MemoryLiteral<String> l = new MemoryLiteral<String>()
                .object("2013")
                .type(IRI.create("xsd:gYear"));
        resource.id(IRI.create("urn:res"))
                .add("urn:property", "Hello World")
                .add("urn:date", l)
                .add("rdf:type", IRI.create("urn:type1"))
                .newResource("urn:embedded")
                .add("rdf:type", IRI.create("urn:type2"));
        resource.newResource("urn:embedded2")
                .add("rdf:type", IRI.create("urn:type3"));
        ResourceContext context = resource.context();
        DefaultContentBuilder defaultContentBuilder = new DefaultContentBuilder();
        String result = defaultContentBuilder.build(context, resource);
        assertEquals(result,
                "{\"urn:property\":\"Hello World\",\"urn:date\":2013,\"rdf:type\":\"urn:type1\",\"urn:embedded\":{\"rdf:type\":\"urn:type2\"},\"urn:embedded2\":{\"rdf:type\":\"urn:type3\"}}");
    }

}
