
package org.xbib.template.handlebars.helper;

import org.xbib.template.handlebars.Helper;
import org.xbib.template.handlebars.Options;

import java.io.IOException;

/**
 * You can use the select helper to conditionally render strings.
 *
 * */
public class SelectHelper implements Helper<Object> {

    /**
     * A singleton instance of this helper.
     */
    public static final Helper<Object> INSTANCE = new SelectHelper();

    /**
     * The helper's name.
     */
    public static final String NAME = "select";

    @Override
    public CharSequence apply(final Object context, final Options options)
            throws IOException {
        if (context == null) {
            return options.fn();
        }
        Object operator = options.param(0);
        Object operand = options.param(1);
        Object pos = options.param(2);
        Object neg = options.param(3);
        Object res;
        switch (operator.toString()) {
            case "==":
                res = context.equals(operand) ? pos : neg;
                break;
            case "!==":
                res = !context.equals(operand) ? pos : neg;
                break;
            case "<":
                res = (Integer)context < (Integer)operand ? pos : neg;
                break;
            case "<=":
                res = (Integer)context <= (Integer)operand ? pos : neg;
                break;
            case ">":
                res = (Integer)context > (Integer)operand ? pos : neg;
                break;
            case ">=":
                res = (Integer)context >= (Integer)operand ? pos : neg;
                break;
            case "&&":
                res = (Boolean)context && (Boolean)operand ? pos : neg;
                break;
            case "||":
                res = (Boolean)context || (Boolean)operand ? pos : neg;
                break;
            default:
                res = options.fn(context);
                break;
        }
        return res.toString();
    }

}
