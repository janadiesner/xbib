
package mustache.specs;

import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.util.Collection;

public class SectionsTest extends SpecTest {

    public SectionsTest(final Spec spec) {
        super(spec);
    }

    @Parameters
    public static Collection<Object[]> data() throws IOException {
        return data("sections.yml");
    }
}
