package org.xbib.rdf.memory;

import java.util.Iterator;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;

public class ResourceTest extends Assert {

    @Test
    public void testResourceId() throws Exception {
        IRI iri = IRI.create("http://index?type#id");
        Resource r = new MemoryResource().id(iri);
        assertEquals("http", r.id().getScheme());
        assertEquals("index", r.id().getHost());
        assertEquals("type", r.id().getQuery());
        assertEquals("id", r.id().getFragment());
    }

    @Test
    public void testEmptyResources() throws Exception {
        Resource r = new MemoryResource().id(IRI.create("urn:root"));
        assertEquals(r.isEmpty(), true);
        assertEquals(r.toString(), "urn:root");
    }

    @Test
    public void testEmptyProperty() throws Exception {
        Resource r = new MemoryResource().id(IRI.create("urn:root"));
        r.add("urn:property", (String) null);
        assertEquals(r.isEmpty(), true);
    }

    @Test
    public void testStringLiteral() throws Exception {
        Resource r = new MemoryResource().id(IRI.create("urn:root"));
        r.add("urn:property", "Hello World");
        assertEquals(r.isEmpty(), false);
        assertEquals(r.triples().iterator().next().object().toString(), "Hello World");
    }
    
    
    @Test
    public void testIntegerLiteral() throws Exception {
        Resource r = new MemoryResource().id(IRI.create("urn:root"));
        MemoryLiteral literal = new MemoryLiteral(123).type(Literal.INT);
        r.add("urn:property", literal);
        assertEquals(r.isEmpty(), false);
        assertEquals(r.triples().iterator().next().object().toString(), "123^^xsd:int");
    }

    @Test
    public void testPredicateSet() throws Exception {
        Resource r = new MemoryResource().id(IRI.create("urn:doc1"))
                .add("urn:valueURI", "Hello World")
                .add("urn:creator", "Smith")
                .add("urn:creator", "Jones");
        Iterator<IRI> it = r.predicates().iterator();
        assertEquals("urn:valueURI", it.next().toString());
        assertEquals("urn:creator", it.next().toString());
        assertFalse(it.hasNext());
    }

    @Test
    public void testObjects() throws Exception {
        Resource r = new MemoryResource().id(IRI.create("urn:doc4"));
        r.add("urn:hasAttribute", "a")
                .add("urn:hasAttribute", "b")
                .add("urn:hasAttribute", "a")
                .add("urn:hasAttribute", "c");
        assertEquals("[a, b, c]", r.objects("urn:hasAttribute").toString());
    }

    @Test
    public void testPropertyIterator() throws Exception {
        Resource r = new MemoryResource();
        String id = "urn:doc2";
        r.id(IRI.create(id))
                .add("urn:valueURI", "Hello World")
                .add("urn:name", "Smith")
                .add("urn:name", "Jones");
        Iterator<Triple> it = r.properties().iterator();
        assertEquals("urn:doc2 urn:valueURI Hello World", it.next().toString());
        assertEquals("urn:doc2 urn:name Smith", it.next().toString());
        assertEquals("urn:doc2 urn:name Jones", it.next().toString());
    }

    @Test
    public void testIterator() throws Exception {
        Resource r = new MemoryResource();
        r.id(IRI.create("urn:doc1"))
                .add("urn:valueURI", "Hello World")
                .add("urn:name", "Smith")
                .add("urn:name", "Jones");
        // the first resource adds a resource value
        Resource r1 = r.newResource("urn:res1");
        r1.add("urn:has", "a first res value");
        // the second resource adds another resource value
        Resource r2 = r.newResource("urn:res1");
        r2.add("urn:has", "a second res value");

        assertEquals(r.predicates().size(), 3);
        
        Iterator<Triple> it = r.triples().iterator();
        assertEquals("urn:doc1 urn:valueURI Hello World", it.next().toString());
        assertEquals("urn:doc1 urn:name Smith", it.next().toString());
        assertEquals("urn:doc1 urn:name Jones", it.next().toString());
        assertEquals("urn:doc1 urn:res1 _:b4", it.next().toString());
        assertEquals("_:b4 urn:has a first res value", it.next().toString());
        assertEquals("urn:doc1 urn:res1 _:b5", it.next().toString());
        assertEquals("_:b5 urn:has a second res value", it.next().toString());
        assertFalse(it.hasNext());
        
        Iterator<IRI> itp = r.predicates().iterator();
        IRI pred = itp.next();
        assertEquals("urn:valueURI", pred.toString());
        Iterator<Node> values = r.objects(pred).iterator();
        assertEquals("Hello World", values.next().toString());
        assertFalse(values.hasNext());
        pred = itp.next();
        assertEquals("urn:name", pred.toString());
        values = r.objects(pred).iterator();
        assertEquals("Smith", values.next().toString());
        assertEquals("Jones", values.next().toString());
        assertFalse(values.hasNext());
        pred = itp.next();
        assertEquals("urn:res1", pred.toString());
        values = r.objects(pred).iterator();
        assertEquals("_:b4", values.next().toString());
        assertEquals("_:b5", values.next().toString());
        assertFalse(values.hasNext());
        assertFalse(itp.hasNext());
    }

    @Test
    public void testCompactPredicate() throws Exception {
        Resource r = new MemoryResource();
        r.id(IRI.create("urn:doc"))
                .add("urn:value1", "Hello World");
        Property predicate = new MemoryProperty(IRI.create("urn:pred"));
        Resource r1 = r.newResource(predicate);
        r1.add(predicate, "a value");
        Iterator<Triple> it = r.triples().iterator();
        int cnt = 0;
        while (it.hasNext()) {
            Triple stmt = it.next();
            cnt++;
        }
        assertEquals(cnt, 3);        
  
        r.compactPredicate(predicate.id());
        it = r.triples().iterator();
        assertEquals("urn:doc urn:value1 Hello World", it.next().toString());
        assertEquals("urn:doc urn:pred a value", it.next().toString());
        assertFalse(it.hasNext());
    }

    @Test
    public void testAddingResources() throws Exception {
        Resource r = new MemoryResource();
        r.id(IRI.create("urn:r"))
                .add("urn:value", "Hello R");

        // named ID
        Resource s = new MemoryResource();
        s.id(IRI.create("urn:s"))
                .add("urn:value", "Hello S");

        // another named ID
        Resource t = new MemoryResource();
        t.id(IRI.create("urn:t"))
                .add("urn:value", "Hello T");

        // a blank node resource ID
        IRI blank1 = new MemoryResource().blank().id();
        Resource u = new MemoryResource();
        u.id(blank1).add("urn:value", "Hello U");

        // another blank node resource ID
        IRI blank2 = new MemoryResource().blank().id();
        Resource v = new MemoryResource();
        v.id(blank2).add("urn:value", "Hello V");

        Property predicate = new MemoryProperty(IRI.create("dc:subject"));
        r.add(predicate, s);
        r.add(predicate, t);
        r.add(predicate, u);
        r.add(predicate, v);

        Iterator<Triple> it = r.triples().iterator();
        assertEquals("urn:r urn:value Hello R", it.next().toString());
        assertEquals("urn:r dc:subject urn:s", it.next().toString());
        assertEquals("urn:s urn:value Hello S", it.next().toString());
        assertEquals("urn:r dc:subject urn:t", it.next().toString());
        assertEquals("urn:t urn:value Hello T", it.next().toString());
        assertEquals("urn:r dc:subject _:b1", it.next().toString());
        assertEquals("_:b1 urn:value Hello U", it.next().toString());
        assertEquals("urn:r dc:subject _:b2", it.next().toString());
        assertEquals("_:b2 urn:value Hello V", it.next().toString());
        assertFalse(it.hasNext());
    }
}
