
package org.xbib.template.handlebars;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xbib.template.handlebars.util.ObjectUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class NoFalsyValueTest {

    /**
     * The value under testing.
     */
    private Object value;

    public NoFalsyValueTest(final Object value) {
        this.value = value;
    }

    @Test
    public void noFalsy() {
        assertEquals(false, ObjectUtil.isEmpty(value));
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[]{new Object()},
                new Object[]{true},
                new Object[]{"Hi"},
                new Object[]{Boolean.TRUE},
                new Object[]{Arrays.asList(1)},
                new Object[]{new Object[]{1}},
                // Custom Iterable
                new Object[]{new Iterable<Integer>() {
                    @Override
                    public Iterator<Integer> iterator() {
                        return Arrays.asList(1).iterator();
                    }
                }});
    }
}
