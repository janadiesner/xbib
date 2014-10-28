package org.xbib.rdf.memory;

import java.util.Iterator;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.Loggers;
import org.xbib.rdf.Identifiable;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;

public class ResourceTest<S extends Identifiable, P extends Property, O extends Node>
        extends Assert {

    private final Logger logger = Loggers.getLogger(ResourceTest.class.getSimpleName());


    private final MemoryFactory<S, P, O> memoryFactory = MemoryFactory.getInstance();

    @Test
    public void testResourceId() throws Exception {
        IRI iri = IRI.create("http://index?type#id");
        Resource<S, P, O> r = new MemoryResource().id(iri);
        assertEquals("http", r.id().getScheme());
        assertEquals("index", r.id().getHost());
        assertEquals("type", r.id().getQuery());
        assertEquals("id", r.id().getFragment());
    }

    @Test
    public void testEmptyResources() throws Exception {
        Resource<S, P, O> r = new MemoryResource().id(IRI.create("urn:root"));
        assertEquals(r.isEmpty(), true);
        assertEquals(r.toString(), "<urn:root>");
    }

    @Test
    public void testEmptyProperty() throws Exception {
        Resource<S, P, O> r = new MemoryResource().id(IRI.create("urn:root"));
        r.add("urn:property", (String) null);
        assertEquals(r.isEmpty(), true);
    }

    @Test
    public void testStringLiteral() throws Exception {
        Resource<S, P, O> r = new MemoryResource().id(IRI.create("urn:root"));
        r.add("urn:property", "Hello World");
        assertEquals(r.isEmpty(), false);
        assertEquals(r.iterator().next().object().toString(), "Hello World");
    }
    
    
    @Test
    public void testIntegerLiteral() throws Exception {
        Resource<S, P, O> r = new MemoryResource().id(IRI.create("urn:root"));
        MemoryLiteral<O> literal = new MemoryLiteral(123).type(Literal.INT);
        r.add("urn:property", literal);
        assertEquals(r.isEmpty(), false);
        assertEquals(r.iterator().next().object().toString(), "123^^xsd:int");
    }

    @Test
    public void testPredicateSet() throws Exception {
        Resource<S, P, O> r = new MemoryResource().id(IRI.create("urn:doc1"))
                .add("urn:valueURI", "Hello World")
                .add("urn:creator", "Smith")
                .add("urn:creator", "Jones");
        S subject = r.subject();
        String[] s = new String[]{"urn:valueURI","urn:creator"};
        int i = 0;
        for (P predicate : r.predicates()) {
            assertEquals(s[i++], predicate.toString());
        }
    }

    @Test
    public void testObjects() throws Exception {
        Resource<S, P, O> r = new MemoryResource().id(IRI.create("urn:doc4"));
        r.add("urn:hasAttribute", "a")
                .add("urn:hasAttribute", "b")
                .add("urn:hasAttribute", "a")
                .add("urn:hasAttribute", "c");
        assertEquals("[a, b, c]", r.objects("urn:hasAttribute").toString());
    }

    @Test
    public void testPropertyIterator() throws Exception {
        Resource<S, P, O> r = new MemoryResource<S, P, O>();
        String id = "urn:doc2";
        r.id(IRI.create(id))
                .add("urn:valueURI", "Hello World")
                .add("urn:name", "Smith")
                .add("urn:name", "Jones");
        Iterator<Triple<S, P, O>> it = r.propertyIterator();
        assertEquals("urn:doc2 urn:valueURI Hello World", it.next().toString());
        assertEquals("urn:doc2 urn:name Smith", it.next().toString());
        assertEquals("urn:doc2 urn:name Jones", it.next().toString());
    }

    @Test
    public void testPredicateSetIterator() throws Exception {
        Resource<S, P, O> r = new MemoryResource();
        r.id(IRI.create("urn:doc1"))
                .add("urn:valueURI", "Hello World")
                .add("urn:name", "Smith")
                .add("urn:name", "Jones");
        // the first resource adds a resource value
        Resource<S, P, O> r1 = r.newResource("urn:res1");
        r1.add("urn:has", "a first res value");
        // the second resource adds another resource value
        Resource<S, P, O> r2 = r.newResource("urn:res1");
        r2.add("urn:has", "a second res value");

        assertEquals(r.predicates().size(), 3);
        
        Iterator<Triple<S,P,O>> it = r.iterator();
        assertEquals("urn:doc1 urn:valueURI Hello World", it.next().toString());
        assertEquals("urn:doc1 urn:name Smith", it.next().toString());
        assertEquals("urn:doc1 urn:name Jones", it.next().toString());
        assertEquals("urn:doc1 urn:res1 <genid:b4>", it.next().toString());
        assertEquals("_:b4 urn:has a first res value", it.next().toString());
        assertEquals("urn:doc1 urn:res1 <genid:b5>", it.next().toString());
        assertEquals("_:b5 urn:has a second res value", it.next().toString());
        assertFalse(it.hasNext());
        
        Iterator<P> itp = r.predicates().iterator();
        int predCounter = 0;
        int objCounter = 0;
        while (itp.hasNext()) {
            P pred = itp.next();
            logger.info("pred=" +pred);
            predCounter++;
            Iterator<O> values = r.objects(pred).iterator();
            while (values.hasNext()) {
                O o = values.next();

                objCounter++;
            }
        }
        /**
         * val=Hello World
         * val=Smith
         * val=Jones
         * val=_:a2 urn:has a first resource value
         * val=_:a3 urn:has a second getRresourcesource value
         */
        assertEquals(objCounter, 5);
        /**
         * pred=urn:valueURI
         * pred=urn:name
         * pred=urn:res1
         */
        assertEquals(predCounter, 3);
    }

    @Test
    public void testCompactPredicate() throws Exception {
        Resource<S, P, O> r = new MemoryResource();
        r.id(IRI.create("urn:doc"))
                .add("urn:value1", "Hello World");
        P predicate = memoryFactory.newPredicate("urn:pred");
        Resource<S, P, O> r1 = r.newResource(predicate);
        r1.add(predicate, "a value");
        Iterator<Triple<S,P,O>> it = r.iterator();
        int cnt = 0;
        while (it.hasNext()) {
            Triple<S,P,O> stmt = it.next();
            cnt++;
        }
        assertEquals(cnt, 3);        
  
        r.compactPredicate(predicate);
        it = r.iterator();
        /*
         * urn:doc urn:value1 Hello World 
         * urn:doc urn:pred a value
         */
        assertEquals("urn:doc urn:value1 Hello World", it.next().toString());
        assertEquals("urn:doc urn:pred a value", it.next().toString());
        assertFalse(it.hasNext());
    }


    @Test
    public void testAddingResources() throws Exception {
        Resource<S, P, O> r = new MemoryResource<S, P, O>();
        r.id(IRI.create("urn:r"))
                .add("urn:value", "Hello R");

        // named ID
        Resource<S, P, O> s = new MemoryResource<S, P, O>();
        s.id(IRI.create("urn:s"))
                .add("urn:value", "Hello S");

        // another named ID
        Resource<S, P, O> t = new MemoryResource<S, P, O>();
        t.id(IRI.create("urn:t"))
                .add("urn:value", "Hello T");

        // a blank node resource ID
        IRI blank1 = new MemoryNode().blank().id();
        Resource<S, P, O> u = new MemoryResource<S, P, O>();
        u.id(blank1).add("urn:value", "Hello U");

        // another blank node resource ID
        IRI blank2 = new MemoryNode().blank().id();
        Resource<S, P, O> v = new MemoryResource<S, P, O>();
        v.id(blank2).add("urn:value", "Hello V");

        P predicate = memoryFactory.newPredicate("dc:subject");
        r.add(predicate, s);
        r.add(predicate, t);
        r.add(predicate, u);
        r.add(predicate, v);

        Iterator<Triple<S,P,O>> it = r.iterator();
        assertEquals("urn:r urn:value Hello R", it.next().toString());
        assertEquals("urn:r dc:subject <urn:s>", it.next().toString());
        assertEquals("urn:s urn:value Hello S", it.next().toString());
        assertEquals("urn:r dc:subject <urn:t>", it.next().toString());
        assertEquals("urn:t urn:value Hello T", it.next().toString());
        assertEquals("urn:r dc:subject <genid:b1>", it.next().toString());
        assertEquals("_:b1 urn:value Hello U", it.next().toString());
        assertEquals("urn:r dc:subject <genid:b2>", it.next().toString());
        assertEquals("_:b2 urn:value Hello V", it.next().toString());
        assertFalse(it.hasNext());
    }
}
