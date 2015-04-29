package org.xbib.xml.util;

import org.testng.annotations.Test;
import org.xbib.xml.XMLUtil;

import static org.testng.Assert.assertEquals;

public class XMLUtilTest {

    @Test
    public void testWhitespaceCleaner() {
        String s = "Hello World\u001b";
        assertEquals(XMLUtil.sanitize(s), "Hello World");
    }

    @Test
    public void testWhitespaceCleanerWithReplacementCharacter() {
        String s = "Hello World\u001b";
        assertEquals(XMLUtil.sanitizeXml10(s), "Hello World�");
    }
}
