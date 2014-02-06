
package mustache.specs;

import org.junit.internal.AssumptionViolatedException;

public class SkipTestException extends AssumptionViolatedException {

    public SkipTestException(final String assumption) {
        super(assumption);
    }

}
