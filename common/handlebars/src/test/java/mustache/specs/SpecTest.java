
package mustache.specs;

import org.junit.Before;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xbib.template.handlebars.Blog;
import org.xbib.template.handlebars.Comment;
import org.xbib.template.handlebars.Handlebars;
import org.xbib.template.handlebars.HandlebarsException;
import org.xbib.template.handlebars.HelperRegistry;
import org.xbib.template.handlebars.Template;
import org.xbib.template.handlebars.util.StringUtil;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;


@RunWith(SpecRunner.class)
public abstract class SpecTest {

    private Spec spec;

    public SpecTest(final Spec spec) {
        this.spec = spec;
    }

    @Test
    public void run() throws HandlebarsException, IOException {
        if (!skip(spec)) {
            run(alter(spec));
        } else {
            throw new SkipTestException(spec.name());
        }
    }

    public static Collection<Object[]> data(final String filename) throws IOException {
        return data(SpecTest.class, filename);
    }

    public static String path(final Class<?> loader) {
        return "/" + loader.getPackage().getName().replace(".", "/") + "/";
    }

    @SuppressWarnings("unchecked")
    public static Collection<Object[]> data(final Class<?> loader,
                                            final String filename) throws IOException {
        Constructor constructor = new Constructor();
        constructor.addTypeDescription(new TypeDescription(Blog.class, "!blog"));
        constructor.addTypeDescription(new TypeDescription(Comment.class, "!comment"));
        constructor.addTypeDescription(new TypeDescription(Map.class, "!code"));

        Yaml yaml = new Yaml(constructor);

        String location = path(loader) + filename;
        String input = StringUtil.stream2String(new FileInputStream(new File("src/test/resources", location)), -1);
        Map<String, Object> data = (Map<String, Object>) yaml.load(input);
        List<Map<String, Object>> tests =
                (List<Map<String, Object>>) data.get("tests");
        int number = 0;
        Collection<Object[]> dataset = new ArrayList<Object[]>();
        for (Map<String, Object> test : tests) {
            test.put("number", number++);
            dataset.add(new Object[]{new Spec(test)});
        }
        return dataset;
    }

    protected boolean skip(final Spec spec) {
        return false;
    }

    protected Spec alter(final Spec spec) {
        return spec;
    }

    private void run(final Spec spec) throws IOException {
        final String input = spec.template();
        final String expected = spec.expected();
        Object data = spec.data();
        Handlebars handlebars = new Handlebars(new SpecResourceLocator(spec));
        handlebars.setPrettyPrint(true);
        configure(handlebars);
        Template template = handlebars.compile("template");
        CharSequence output = template.apply(data);
        try {
            assertEquals(expected, output);
        } catch (HandlebarsException | ComparisonFailure ex) {
            throw ex;
        }
    }

    protected HelperRegistry configure(final Handlebars handlebars) {
        return handlebars;
    }

    @Before
    public void initJUnit() throws IOException {
        // Init junit classloader. This reduce the time reported during execution.
        new Handlebars();
    }

}
