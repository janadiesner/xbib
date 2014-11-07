package org.xbib.rdf.xcontent;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.rdf.Resource;
import org.xbib.rdf.content.DefaultContentBuilder;
import org.xbib.rdf.Context;
import org.xbib.rdf.memory.MemoryLiteral;
import org.xbib.rdf.memory.MemoryResource;
import org.xbib.rdf.memory.MemoryContext;

public class DefaultContentBuilderTest
    extends Assert {

    @Test
    public void testContentBuilder() throws Exception {
        Resource resource = new MemoryResource();
        MemoryLiteral l = new MemoryLiteral("2013")
                .type(IRI.create("xsd:gYear"));
        resource.id(IRI.create("urn:res"))
                .add("urn:property", "Hello World")
                .add("urn:date", l)
                .add("urn:link", IRI.create("urn:pointer"));
        Context context = new MemoryContext<>();
        DefaultContentBuilder defaultContentBuilder = new DefaultContentBuilder();
        String result = defaultContentBuilder.build(context, resource);
        assertEquals(result,
                "{\"urn:property\":\"Hello World\",\"urn:date\":2013,\"urn:link\":\"urn:pointer\"}");
    }

    @Test
    public void testContentBuilderSingleEmbedded() throws Exception {
        Resource resource = new MemoryResource();
        MemoryLiteral l = new MemoryLiteral("2013")
                .type(IRI.create("xsd:gYear"));
        resource.id(IRI.create("urn:res"))
                .add("urn:property", "Hello World")
                .add("urn:date", l)
                .add("rdf:type", IRI.create("urn:type1"))
                .newResource("urn:embedded")
                .add("rdf:type", IRI.create("urn:type2"));
        Context context = new MemoryContext<>();
        DefaultContentBuilder defaultContentBuilder = new DefaultContentBuilder();
        String result = defaultContentBuilder.build(context, resource);
        assertEquals(result,
                "{\"urn:property\":\"Hello World\",\"urn:date\":2013,\"rdf:type\":\"urn:type1\",\"urn:embedded\":{\"rdf:type\":\"urn:type2\"}}");
    }

    @Test
    public void testContentBuilderDoubleEmbedded() throws Exception {
        Resource resource = new MemoryResource();
        MemoryLiteral l = new MemoryLiteral("2013")
                .type(IRI.create("xsd:gYear"));
        resource.id(IRI.create("urn:res"))
                .add("urn:property", "Hello World")
                .add("urn:date", l)
                .add("rdf:type", IRI.create("urn:type1"))
                .newResource("urn:embedded")
                .add("rdf:type", IRI.create("urn:type2"));
        resource.newResource("urn:embedded2")
                .add("rdf:type", IRI.create("urn:type3"));
        Context context = new MemoryContext<>();
        DefaultContentBuilder defaultContentBuilder = new DefaultContentBuilder();
        String result = defaultContentBuilder.build(context, resource);
        assertEquals(result,
                "{\"urn:property\":\"Hello World\",\"urn:date\":2013,\"rdf:type\":\"urn:type1\",\"urn:embedded\":{\"rdf:type\":\"urn:type2\"},\"urn:embedded2\":{\"rdf:type\":\"urn:type3\"}}");
    }

}
