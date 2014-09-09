
package org.xbib.template.handlebars;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xbib.template.handlebars.io.ClassPathTemplateLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class InheritanceTest {

    static Handlebars handlebars =
            new Handlebars(new ClassPathTemplateLoader("/inheritance"));

    static {
        handlebars.setPrettyPrint(true);
    }

    private String name;

    public InheritanceTest(final String name) {
        this.name = name;
    }

    @Test
    public void inheritance() throws IOException {
        try {
            Template template = handlebars.compile(name);
            CharSequence result = template.apply(new Object());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = getClass().getResourceAsStream("/inheritance/" + name + ".expected");
            copy(in, out, 1024);
            String expected = new String(out.toByteArray());
            assertEquals(expected, result);
        } catch (HandlebarsException ex) {
            //Handlebars.error(ex.getMessage());
            throw ex;
        }
    }

    @Parameters
    public static Collection<Object[]> data() {
        Collection<Object[]> data =
                Arrays.asList(new Object[]{"home"}, new Object[]{"about"},
                        new Object[]{"base"});
        return data;
    }

    static String toString(final InputStream input)
            throws IOException {
        StringBuilder buffer = new StringBuilder(1024 * 4);
        int ch;
        while ((ch = input.read()) != -1) {
            buffer.append((char) ch);
        }
        buffer.trimToSize();
        input.close();
        return buffer.toString();
    }

    private static int copy(InputStream input, OutputStream output, int bufSize) throws IOException {
        byte[] buffer = new byte[bufSize];
        int count = 0;
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
