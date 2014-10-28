package org.xbib.rdf.memory;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.rdf.Identifiable;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;

public class TripleTest extends Assert {

    @Test
    public void testSimpleTriple() {
        Identifiable s = new MemoryNode().id(IRI.create("urn:1"));
        Property p = new MemoryProperty(IRI.create("urn:2"));
        Node o = new MemoryLiteral("Hello World");
        MemoryTriple triple = new MemoryTriple(s, p, o);
        assertEquals(triple.subject().id(), s.id());
        assertEquals(triple.predicate().id(), p.id());
        assertEquals(triple.object(), o);
    }
}
