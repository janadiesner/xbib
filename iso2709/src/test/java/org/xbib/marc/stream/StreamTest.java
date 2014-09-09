package org.xbib.marc.stream;

import org.testng.annotations.Test;
import org.xbib.io.field.Separable;
import org.xbib.marc.Iso2709Reader;

import java.io.IOException;
import java.util.function.Consumer;

import static org.testng.Assert.assertEquals;

public class StreamTest {

    @Test
    public void testZDB() throws IOException {
        Iso2709Reader reader = new Iso2709Reader();
        long count = reader.stream(getClass().getResource("zdblokutf8.mrc").openStream())
                .fields()
                .count();
        assertEquals(10171L, count);
        reader.stream(getClass().getResource("zdblokutf8.mrc").openStream())
                .fields()
                .forEach(new Consumer<Separable>() {
                    @Override
                    public void accept(Separable s) {
                        // TODO
                    }
                });
    }
}
