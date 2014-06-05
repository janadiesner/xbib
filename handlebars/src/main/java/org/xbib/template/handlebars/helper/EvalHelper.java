
package org.xbib.template.handlebars.helper;

import org.xbib.template.handlebars.Helper;
import org.xbib.template.handlebars.Options;

import java.io.IOException;

/**
 * You can use the eval helper to evaluate binary expressions.
 */
public class EvalHelper implements Helper<Object> {

    /**
     * A singleton instance of this helper.
     */
    public static final Helper<Object> INSTANCE = new EvalHelper();

    /**
     * The helper's name.
     */
    public static final String NAME = "eval";

    @Override
    public CharSequence apply(final Object context, final Options options)
            throws IOException {
        Object operator = options.param(0);
        Object operand = options.param(1);
        Object res;
        String s = operator.toString();
        if (s.equals("+")) {
            res = (Integer) context + (Integer) operand;

        } else if (s.equals("-")) {
            res = (Integer) context - (Integer) operand;

        } else if (s.equals("*")) {
            res = (Integer) context * (Integer) operand;

        } else if (s.equals("/")) {
            res = (Integer) context / (Integer) operand;

        } else if (s.equals("%")) {
            res = (Integer) context % (Integer) operand;

        } else {
            res = options.fn(context);
        }
        return res.toString();
    }
}
