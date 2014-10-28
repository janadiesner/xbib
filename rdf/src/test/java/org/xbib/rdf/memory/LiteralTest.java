package org.xbib.rdf.memory;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.Loggers;

public class LiteralTest extends Assert {

    private final Logger logger = Loggers.getLogger(LiteralTest.class);

    @Test
    public void testLiteral() {
        MemoryLiteral<String> l = new MemoryLiteral<String>()
                .object("2013")
                .type(IRI.create("xsd:gYear"));
        assertEquals(l.toString(), "2013^^xsd:gYear");
        assertEquals(l.value(), 2013);
    }
}
