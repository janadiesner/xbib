package org.xbib.rdf.memory;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.rdf.Node;
import org.xbib.rdf.Resource;

public class TripleTest extends Assert {

    @Test
    public void testSimpleTriple() {
        Resource s = new MemoryResource().id(IRI.create("urn:1"));
        IRI p = IRI.create("urn:2");
        Node o = new MemoryLiteral("Hello World");
        MemoryTriple triple = new MemoryTriple(s, p, o);
        assertEquals(triple.subject().id(), s.id());
        assertEquals(triple.predicate(), p);
        assertEquals(triple.object(), o);
    }
}
