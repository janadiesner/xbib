
package mustache.specs;

import org.junit.runners.Parameterized.Parameters;
import org.xbib.template.handlebars.Handlebars;
import org.xbib.template.handlebars.HelperRegistry;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * There are 4 tests what don't work as the spec says:
 * <ul>
 * <li>1. Failed Lookup. This tests look for a partial named: 'text', the
 * partial isn't defined and cannot be loaded. The spec says it should default
 * to an empty string. Handlebars.java throw an exception if a template cannot
 * be loaded.
 * </ul>
 */
public class PartialsTest extends SpecTest {

    public PartialsTest(final Spec spec) {
        super(spec);
    }

    @Override
    protected boolean skip(final Spec spec) {
        List<Integer> skip = Arrays.asList(1);
        return skip.contains(spec.number());
    }

    @Override
    protected HelperRegistry configure(final Handlebars handlebars) {
        handlebars.setInfiniteLoops(true);
        return super.configure(handlebars);
    }

    @Parameters
    public static Collection<Object[]> data() throws IOException {
        return data("partials.yml");
    }
}
