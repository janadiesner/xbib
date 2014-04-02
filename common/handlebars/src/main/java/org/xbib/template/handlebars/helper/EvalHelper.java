
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
        return eval(options).fn();
    }

    private Options eval(Options options) {
        Object v1 = options.param(0);
        Object operator = options.param(1);
        Object v2 = options.param(2);
        Object res;
        switch (operator.toString()) {
            case "+":
                res = (Long)v1 + (Long)v2;
                break;
            case "-":
                res = (Long)v1 - (Long)v2;
                break;
            case "*":
                res = (Long)v1 * (Long)v2;
                break;
            case "/":
                res = (Long)v1 / (Long)v2;
                break;
            case "%":
                res = (Long)v1 % (Long)v2;
                break;
            default:
                res = v1;
        }
        return new Options.Builder(options).setParams(new Object[]{res}).build();
    }
}
