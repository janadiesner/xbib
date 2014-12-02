package org.xbib.common.xcontent;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.common.settings.Settings;
import org.xbib.common.xcontent.xml.XmlXParams;
import org.xbib.xml.namespace.XmlNamespaceContext;

import javax.xml.namespace.QName;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;

import static com.google.common.io.ByteStreams.copy;
import static org.xbib.common.settings.ImmutableSettings.settingsBuilder;
import static org.xbib.common.xcontent.XContentFactory.xmlBuilder;

public class XContentXmlBuilderTest extends Assert {

    @Test
    public void testXml() throws Exception {
        XContentBuilder builder = xmlBuilder();
        builder.startObject().field("Hello", "World").endObject();
        assertEquals(builder.string(),
                "<root xmlns=\"http://elasticsearch.org/ns/1.0/\" xmlns:es=\"http://elasticsearch.org/ns/1.0/\"><Hello>World</Hello></root>"
        );
    }

    @Test
    public void testXmlParams() throws Exception {
        XmlXParams params = new XmlXParams();
        XContentBuilder builder = xmlBuilder(params);
        builder.startObject().field("Hello", "World").endObject();
        assertEquals(builder.string(),
                "<root xmlns=\"http://elasticsearch.org/ns/1.0/\" xmlns:es=\"http://elasticsearch.org/ns/1.0/\"><Hello>World</Hello></root>"
        );
    }

    @Test
    public void testXmlNamespaces() throws Exception {
        XmlNamespaceContext context = XmlNamespaceContext.getDefaultInstance();
        XmlXParams params = new XmlXParams(context);
        XContentBuilder builder = xmlBuilder(params);
        builder.startObject()
                .field("dc:creator", "John Doe")
                .endObject();
        assertEquals(builder.string(),
                "<root xmlns=\"http://elasticsearch.org/ns/1.0/\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:es=\"http://elasticsearch.org/ns/1.0/\" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:xalan=\"http://xml.apache.org/xslt\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"><dc:creator>John Doe</dc:creator></root>"
        );
    }

    @Test
    public void testXmlCustomNamespaces() throws Exception {
        QName root = new QName("result");
        XmlNamespaceContext context = XmlNamespaceContext.getDefaultInstance();
        context.addNamespace("abc", "http://localhost");
        XmlXParams params = new XmlXParams(root, context);
        XContentBuilder builder = xmlBuilder(params);
        builder.startObject()
                .field("abc:creator", "John Doe")
                .endObject();
        assertEquals(builder.string(),
                "<result xmlns:abc=\"http://localhost\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:es=\"http://elasticsearch.org/ns/1.0/\" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:xalan=\"http://xml.apache.org/xslt\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"><abc:creator>John Doe</abc:creator></result>"
        );
    }

    @Test
    public void testXmlObject() throws Exception {
        QName root = XmlXParams.getDefaultParams().getQName();
        XmlXParams params = new XmlXParams(root);
        XContentBuilder builder = xmlBuilder(params);
        builder.startObject()
                .startObject("author")
                .field("creator", "John Doe")
                .field("role", "writer")
                .endObject()
                .startObject("author")
                .field("creator", "Joe Smith")
                .field("role", "illustrator")
                .endObject()
                .endObject();
        assertEquals(builder.string(),
            "<root xmlns=\"http://elasticsearch.org/ns/1.0/\" xmlns:es=\"http://elasticsearch.org/ns/1.0/\"><author><creator>John Doe</creator><role>writer</role></author><author><creator>Joe Smith</creator><role>illustrator</role></author></root>"
        );

    }

    @Test
    public void testXmlAttributes() throws Exception {
        QName root = XmlXParams.getDefaultParams().getQName();
        XmlXParams params = new XmlXParams(root);
        XContentBuilder builder = xmlBuilder(params);
        builder.startObject()
                .startObject("author")
                .field("@name", "John Doe")
                .field("@id", 1)
                .endObject()
                .endObject();
        assertEquals(builder.string(),
            "<root xmlns=\"http://elasticsearch.org/ns/1.0/\" xmlns:es=\"http://elasticsearch.org/ns/1.0/\"><author><name>John Doe</name><id>1</id></author></root>"
        );
    }


    @Test
    public void testXmlArrayOfValues() throws Exception {
        QName root = XmlXParams.getDefaultParams().getQName();
        XmlXParams params = new XmlXParams(root);
        XContentBuilder builder = xmlBuilder(params);
        builder.startObject()
                .array("author", "John Doe", "Joe Smith")
                .endObject();
        assertEquals(builder.string(),
            "<root xmlns=\"http://elasticsearch.org/ns/1.0/\" xmlns:es=\"http://elasticsearch.org/ns/1.0/\"><author>John Doe</author><author>Joe Smith</author></root>"
        );
    }

    @Test
    public void testXmlArrayOfObjects() throws Exception {
        QName root = XmlXParams.getDefaultParams().getQName();
        XmlXParams params = new XmlXParams(root);
        XContentBuilder builder = xmlBuilder(params);
        builder.startObject()
                .startArray("author")
                .startObject()
                .field("creator", "John Doe")
                .field("role", "writer")
                .endObject()
                .startObject()
                .field("creator", "Joe Smith")
                .field("role", "illustrator")
                .endObject()
                .endArray()
                .endObject();
        assertEquals(builder.string(),
                "<root xmlns=\"http://elasticsearch.org/ns/1.0/\" xmlns:es=\"http://elasticsearch.org/ns/1.0/\"><author><creator>John Doe</creator><role>writer</role></author><author><creator>Joe Smith</creator><role>illustrator</role></author></root>"
        );
    }

    @Test
    public void testParseJson() throws Exception {
        XmlNamespaceContext context = XmlNamespaceContext.getDefaultInstance();
        context.addNamespace("bib","info:srw/cql-context-set/1/bib-v1/");
        context.addNamespace("xbib", "http://xbib.org/");
        context.addNamespace("abc", "http://localhost/");
        context.addNamespace("lia", "http://xbib.org/namespaces/lia/");
        XmlXParams params = new XmlXParams(context);
        InputStream in = getClass().getResourceAsStream("/org/xbib/common/xcontent/dc.json");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out);
        byte[] buf = out.toByteArray();
    }

    @Test
    public void testParseReader() throws Exception {
        StringReader sr = new StringReader("{\"name\":\"value\"}");
        Settings settings = settingsBuilder()
                .loadFromReader(sr).build();
    }

}
