package org.xbib.xml.transform;

import org.testng.annotations.Test;
import org.xbib.common.xcontent.xml.XmlNamespaceContext;
import org.xbib.json.JsonXmlReader;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.transform.sax.SAXSource;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class StylesheetTransformerTest {

    private final Logger logger = LoggerFactory.getLogger(StylesheetTransformerTest.class.getName());

    private final QName root = new QName("http://example.org", "result", "ex");

    private XmlNamespaceContext context = XmlNamespaceContext.getDefaultInstance();

    @Test
    public void testJsonAsXML() throws Exception {
        InputStream in = getClass().getResourceAsStream("/org/xbib/json/dc.json");
        if (in == null) {
            throw new IOException("dc.json not found");
        }
        context.addNamespace("xbib", "http://xbib.org/");
        context.addNamespace("bib", "info:bib");
        context.addNamespace("lia", "http://xbib.org/lia/");

        JsonXmlReader reader = new JsonXmlReader()
                .root(root)
                .context(context);
        FileWriter out = new FileWriter("target/dc.json.xml");
        StylesheetTransformer transformer = new StylesheetTransformer(
                "src/main/resources",
                "src/main/resources/xsl");
        // no style sheet, just a simple copy
        transformer.setSource(new SAXSource(reader, new InputSource(in)))
                .setResult(out)
                .transform();
        out.close();
    }
}
