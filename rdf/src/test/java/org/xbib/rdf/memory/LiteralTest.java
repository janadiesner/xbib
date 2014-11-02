package org.xbib.rdf.memory;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;

public class LiteralTest extends Assert {

    @Test
    public void testLiteral() {
        MemoryLiteral l = new MemoryLiteral("2013")
                .type(IRI.create("xsd:gYear"));
        assertEquals(l.toString(), "2013^^xsd:gYear");
        assertEquals(l.object(), 2013);
    }
}
