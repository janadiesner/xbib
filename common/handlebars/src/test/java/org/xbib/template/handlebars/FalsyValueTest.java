
package org.xbib.template.handlebars;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xbib.template.handlebars.util.ObjectUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;


@RunWith(Parameterized.class)
public class FalsyValueTest {

    /**
     * The value under testing.
     */
    private Object value;

    public FalsyValueTest(final Object value) {
        this.value = value;
    }

    @Test
    public void falsy() {
        assertEquals(true, ObjectUtil.isEmpty(value));
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[]{null},
                new Object[]{false},
                new Object[]{""},
                new Object[]{Boolean.FALSE},
                new Object[]{0},
                new Object[]{(short) 0},
                new Object[]{0L},
                new Object[]{0F},
                new Object[]{0D},
                new Object[]{BigInteger.ZERO},
                new Object[]{BigDecimal.ZERO},
                new Object[]{Collections.emptyList()},
                new Object[]{new Object[0]},
                // Custom Iterable
                new Object[]{new Iterable<Object>() {
                    @Override
                    public Iterator<Object> iterator() {
                        return Collections.emptyList().iterator();
                    }
                }});
    }
}
