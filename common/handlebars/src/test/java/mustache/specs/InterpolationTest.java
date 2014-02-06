
package mustache.specs;

import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.util.Collection;

public class InterpolationTest extends SpecTest {

    public InterpolationTest(final Spec spec) {
        super(spec);
    }

    @Parameters
    public static Collection<Object[]> data() throws IOException {
        return data("interpolation.yml");
    }
}
