
package org.xbib.template.handlebars;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HandlebarsContextTest {

    static class Base {
        public String getBaseProperty() {
            return "baseProperty";
        }

        public String getChildProperty() {
            return "baseProperty";
        }
    }

    static class Child extends Base {
        @Override
        public String getChildProperty() {
            return "childProperty";
        }
    }

    @Test
    public void newContext() {
        HandlebarsContext context = HandlebarsContext.newContext("String");
        assertNotNull(context);
        assertEquals("String", context.model());
    }

    @Test(expected = IllegalArgumentException.class)
    public void noContext() {
        Map<String, Object> model = new HashMap<String, Object>();
        HandlebarsContext context = HandlebarsContext.newContext(model);
        assertEquals(context, HandlebarsContext.newContext(context));
    }

    @Test
    public void parentContext() {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("name", "Handlebars");
        HandlebarsContext parent = HandlebarsContext.newContext(model);
        assertNotNull(parent);
        assertEquals("Handlebars", parent.get("name"));

        Map<String, Object> extended = new HashMap<String, Object>();
        extended.put("n", "Extended");
        HandlebarsContext child = HandlebarsContext.newContext(parent, extended);
        assertEquals("Extended", child.get("n"));
        assertEquals("Handlebars", child.get("name"));
    }

    @Test(expected = NullPointerException.class)
    public void nullParent() {
        HandlebarsContext.newContext(null, new Object());
    }

    @Test
    public void dotLookup() {
        HandlebarsContext context = HandlebarsContext.newContext("String");
        assertNotNull(context);
        assertEquals("String", context.get("."));
    }

    @Test
    public void thisLookup() {
        HandlebarsContext context = HandlebarsContext.newContext("String");
        assertNotNull(context);
        assertEquals("String", context.get("this"));
    }

    @Test
    public void singleMapLookup() {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("simple", "value");
        HandlebarsContext context = HandlebarsContext.newContext(model);
        assertNotNull(context);
        assertEquals("value", context.get("simple"));
    }

    @Test
    public void nestedMapLookup() {
        Map<String, Object> model = new HashMap<String, Object>();
        Map<String, Object> nested = new HashMap<String, Object>();
        model.put("nested", nested);
        nested.put("simple", "value");
        HandlebarsContext context = HandlebarsContext.newContext(model);
        assertNotNull(context);
        assertEquals("value", context.get("nested.simple"));
    }

    @Test
    public void singleObjectLookup() {
        Object model = new Object() {
            @SuppressWarnings("unused")
            public String getSimple() {
                return "value";
            }

            @Override
            public String toString() {
                return "Model Object";
            }
        };
        HandlebarsContext context = HandlebarsContext.newContext(model);
        assertNotNull(context);
        assertEquals("value", context.get("simple"));
    }

    @Test
    public void nestedObjectLookup() {
        Object model = new Object() {
            @SuppressWarnings("unused")
            public Object getNested() {
                return new Object() {
                    public String getSimple() {
                        return "value";
                    }
                };
            }
        };
        HandlebarsContext context = HandlebarsContext.newContext(model);
        assertNotNull(context);
        assertEquals("value", context.get("nested.simple"));
    }

    @Test
    public void customLookup() {
        HandlebarsContext context = HandlebarsContext.newContext(new Base());
        assertNotNull(context);
        assertEquals("baseProperty", context.get("baseProperty"));
        assertEquals("baseProperty", context.get("childProperty"));
    }

    @Test
    public void customLookupOnChildClass() {
        HandlebarsContext context = HandlebarsContext.newContext(new Child());
        assertNotNull(context);
        assertEquals("baseProperty", context.get("baseProperty"));
        assertEquals("childProperty", context.get("childProperty"));
    }

    @Test
    public void combine() {
        HandlebarsContext context = HandlebarsContext
                .newBuilder(new Base())
                .combine("expanded", "value")
                .build();
        assertNotNull(context);
        assertEquals("baseProperty", context.get("baseProperty"));
        assertEquals("value", context.get("expanded"));
    }

    @Test
    public void contextResolutionInCombine() {
        HandlebarsContext context = HandlebarsContext
                .newBuilder(new Base())
                .combine("baseProperty", "value")
                .build();
        assertNotNull(context);
        assertEquals("baseProperty", context.get("baseProperty"));
    }

    @Test
    public void combineNested() {
        Map<String, Object> expanded = new HashMap<String, Object>();
        expanded.put("a", "a");
        expanded.put("b", true);
        HandlebarsContext context = HandlebarsContext
                .newBuilder(new Base())
                .combine("expanded", expanded)
                .build();
        assertNotNull(context);
        assertEquals("baseProperty", context.get("baseProperty"));
        assertEquals(expanded, context.get("expanded"));
        assertEquals("a", context.get("expanded.a"));
        assertEquals(true, context.get("expanded.b"));
    }

    @Test
    public void expanded() {
        Map<String, Object> expanded = new HashMap<String, Object>();
        expanded.put("a", "a");
        expanded.put("b", true);
        HandlebarsContext context = HandlebarsContext
                .newBuilder(new Base())
                .combine(expanded)
                .build();
        assertNotNull(context);
        assertEquals("baseProperty", context.get("baseProperty"));
        assertEquals(null, context.get("expanded"));
        assertEquals("a", context.get("a"));
        assertEquals(true, context.get("b"));
    }

    @Test
    public void issue28() {
        HandlebarsContext root = HandlebarsContext.newBuilder("root").build();
        assertEquals("root", root.get("this"));
        HandlebarsContext child1 = HandlebarsContext.newBuilder(root, "child1").build();
        assertEquals("child1", child1.get("this"));
        HandlebarsContext child2 =
                HandlebarsContext.newBuilder(root, "child2")
                        .combine(new HashMap<String, Object>()).build();
        assertEquals("child2", child2.get("this"));
    }

    public void testCombineGenerics() {

        HandlebarsContext.newBuilder("blah").combine(new HashMap<String, String>());
        HandlebarsContext.newBuilder("blah").combine(new HashMap<String, Object>());
        HandlebarsContext.newBuilder("blah").combine(new HashMap<String, Map<String, Object>>());
    }

}
