package org.xbib.json.pointer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.xbib.json.jackson.JacksonUtils;
import org.xbib.json.jackson.NodeType;
import org.xbib.json.jackson.SampleNodeProvider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public final class JsonNodeResolverTest {
    private static final JsonNodeFactory FACTORY = JacksonUtils.nodeFactory();

    @Test
    public void resolvingNullReturnsNull() {
        final JsonNodeResolver resolver
                = new JsonNodeResolver(ReferenceToken.fromRaw("whatever"));

        assertNull(resolver.get(null));
    }

    @DataProvider
    public Iterator<Object[]> nonContainers() {
        return SampleNodeProvider.getSamplesExcept(NodeType.ARRAY,
                NodeType.OBJECT);
    }

    @Test(dataProvider = "nonContainers")
    public void resolvingNonContainerNodeReturnsNull(final JsonNode node) {
        final JsonNodeResolver resolver
                = new JsonNodeResolver(ReferenceToken.fromRaw("whatever"));

        assertNull(resolver.get(node));
    }

    @Test
    public void resolvingObjectMembersWorks() {
        final JsonNodeResolver resolver
                = new JsonNodeResolver(ReferenceToken.fromRaw("a"));
        final JsonNode target = FACTORY.textNode("b");

        ObjectNode node;

        node = FACTORY.objectNode();
        node.put("a", target);

        final JsonNode resolved = resolver.get(node);
        assertEquals(resolved, target);

        node = FACTORY.objectNode();
        node.put("b", target);

        assertNull(resolver.get(node));
    }

    @Test
    public void resolvingArrayIndicesWorks() {
        final JsonNodeResolver resolver
                = new JsonNodeResolver(ReferenceToken.fromInt(1));

        final JsonNode target = FACTORY.textNode("b");
        final ArrayNode node = FACTORY.arrayNode();

        node.add(target);
        assertNull(resolver.get(node));

        node.add(target);
        assertEquals(target, resolver.get(node));
    }

    @DataProvider
    public Iterator<Object[]> invalidIndices() {
        final List<Object[]> list = new ArrayList();

        list.add(new Object[]{"-1"});
        list.add(new Object[]{"232398087298731987987232"});
        list.add(new Object[]{"00"});
        list.add(new Object[]{"0 "});
        list.add(new Object[]{" 0"});

        return list.iterator();
    }


    @Test(dataProvider = "invalidIndices")
    public void invalidIndicesYieldNull(final String raw) {
        final JsonNode target = FACTORY.textNode("b");
        final ArrayNode node = FACTORY.arrayNode();

        node.add(target);

        final ReferenceToken refToken = ReferenceToken.fromRaw(raw);
        final JsonNodeResolver resolver = new JsonNodeResolver(refToken);
        assertNull(resolver.get(node));
    }
}
