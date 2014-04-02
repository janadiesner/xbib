
package org.xbib.template.handlebars;

import org.junit.Test;
import org.xbib.template.handlebars.context.FieldValueResolver;
import org.xbib.template.handlebars.context.JavaBeanValueResolver;
import org.xbib.template.handlebars.context.MapValueResolver;
import org.xbib.template.handlebars.context.MethodValueResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Unit test for {@link HandlebarsContext}.
 */
public class ValueResolverTest {

    static class Base {

        String base;

        String child;

        public Base(final String base, final String child) {
            this.base = base;
            this.child = child;
        }

        public String getBaseProperty() {
            return base;
        }

        public String getChildProperty() {
            return child;
        }
    }

    @Test
    public void javaBeanResolver() {
        HandlebarsContext context = HandlebarsContext
                .newBuilder(new Base("a", "b"))
                .resolver(JavaBeanValueResolver.INSTANCE)
                .build();
        assertNotNull(context);
        assertEquals("a", context.get("baseProperty"));
        assertEquals("b", context.get("childProperty"));
    }

    @Test
    public void methodResolver() {
        HandlebarsContext context = HandlebarsContext
                .newBuilder(new Base("a", "b"))
                .resolver(MethodValueResolver.INSTANCE)
                .build();
        assertNotNull(context);
        assertEquals("a", context.get("getBaseProperty"));
        assertEquals("b", context.get("getChildProperty"));
    }

    @Test
    public void fieldResolver() {
        HandlebarsContext context = HandlebarsContext
                .newBuilder(new Base("a", "b"))
                .resolver(FieldValueResolver.INSTANCE)
                .build();
        assertNotNull(context);
        assertEquals("a", context.get("base"));
        assertEquals("b", context.get("child"));
    }

    @Test
    public void mapResolver() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("base", "a");
        map.put("child", "b");

        HandlebarsContext context = HandlebarsContext
                .newBuilder(map)
                .resolver(MapValueResolver.INSTANCE)
                .build();
        assertNotNull(context);
        assertEquals("a", context.get("base"));
        assertEquals("b", context.get("child"));
    }

    @Test
    public void multipleValueResolvers() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("base", "a");
        map.put("child", "b");

        HandlebarsContext context =
                HandlebarsContext
                        .newBuilder(new Base("a", "b"))
                        .combine("map", map)
                        .resolver(
                                MapValueResolver.INSTANCE,
                                JavaBeanValueResolver.INSTANCE,
                                MethodValueResolver.INSTANCE,
                                FieldValueResolver.INSTANCE)
                        .build();
        assertNotNull(context);
        // by field
        assertEquals("a", context.get("base"));
        assertEquals("b", context.get("child"));
        // by javaBean
        assertEquals("a", context.get("baseProperty"));
        assertEquals("b", context.get("childProperty"));
        // by method name
        assertEquals("a", context.get("getBaseProperty"));
        assertEquals("b", context.get("getChildProperty"));
        // by map
        assertEquals("a", context.get("map.base"));
        assertEquals("b", context.get("map.child"));
    }

    @Test
    public void propagateValueResolverToChild() throws IOException {
        final Object userFiledAccess = new Object() {
            @SuppressWarnings("unused")
            private String name = "User A";
        };

        final Object userMethodAccess = new Object() {
            @SuppressWarnings("unused")
            public String getName() {
                return "User B";
            }
        };

        Object users = new Object() {
            @SuppressWarnings("unused")
            public List<Object> getUsers() {
                return Arrays.asList(userFiledAccess, userMethodAccess);
            }
        };

        Template template =
                new Handlebars().compileInline("{{#each users}}{{name}}, {{/each}}");

        HandlebarsContext context = HandlebarsContext.newBuilder(users)
                .resolver(
                        FieldValueResolver.INSTANCE,
                        JavaBeanValueResolver.INSTANCE
                )
                .build();

        assertEquals("User A, User B, ", template.apply(context));
    }

    @Test
    public void propagateValueResolverToChildAndExtended() throws IOException {
        final Object userFiledAccess = new Object() {
            @SuppressWarnings("unused")
            private String name = "User A";
        };

        final Object extended = new Object() {
            @SuppressWarnings("unused")
            private String role = "role";
        };

        final Object userMethodAccess = new Object() {
            @SuppressWarnings("unused")
            public String getName() {
                return "User B";
            }
        };

        Object users = new Object() {
            @SuppressWarnings("unused")
            public List<Object> getUsers() {
                return Arrays.asList(userFiledAccess, userMethodAccess);
            }
        };

        Template template =
                new Handlebars().compileInline("{{#each users}}{{name}}-{{extended.role}}, {{/each}}");

        HandlebarsContext context = HandlebarsContext.newBuilder(users)
                .combine("extended", extended)
                .resolver(
                        MapValueResolver.INSTANCE,
                        FieldValueResolver.INSTANCE,
                        JavaBeanValueResolver.INSTANCE
                )
                .build();

        assertEquals("User A-role, User B-role, ", template.apply(context));
    }
}
