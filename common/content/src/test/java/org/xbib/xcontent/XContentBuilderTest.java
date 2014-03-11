package org.xbib.xcontent;

import org.testng.annotations.Test;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.logging.Logger;
import org.xbib.logging.Loggers;

import java.io.IOException;
import java.util.Arrays;

import static org.testng.Assert.assertEquals;
import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

public class XContentBuilderTest {

    private final Logger logger = Loggers.getLogger(XContentBuilderTest.class);

    @Test
    public void testCopy() throws IOException {
        XContentBuilder builder = jsonBuilder();
        builder.startObject().field("hello", "world").endObject();
        builder.close();

        XContentBuilder builder2 = jsonBuilder();
        builder2.copy(builder);
        builder2.close();
        assertEquals("{\"hello\":\"world\"}", builder2.string());
    }


    @Test
    public void testCopyList() throws IOException {
        XContentBuilder builder1 = jsonBuilder();
        builder1.startObject().field("hello", "world").endObject();
        builder1.close();
        XContentBuilder builder2 = jsonBuilder();
        builder2.startObject().field("hello", "world").endObject();
        builder2.close();

        XContentBuilder builder = jsonBuilder();
        builder.startObject().startArray("list");
        builder.copy(Arrays.asList(builder1, builder2));
        builder.endArray().endObject();
        builder.close();

        assertEquals("{\"list\":[{\"hello\":\"world\"},{\"hello\":\"world\"}]}", builder.string());
    }

}
