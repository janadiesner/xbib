
package org.xbib.template.handlebars.helper;

import org.xbib.template.handlebars.Helper;
import org.xbib.template.handlebars.Options;

import java.io.IOException;

/**
 * You can use the ifCond helper to conditionally render a block. If its argument
 * returns false, Handlebars will not render the block.
 */
public class IfConditionHelper implements Helper<Object> {

    /**
     * A singleton instance of this helper.
     */
    public static final Helper<Object> INSTANCE = new IfConditionHelper();

    /**
     * The helper's name.
     */
    public static final String NAME = "ifCond";

    @Override
    public CharSequence apply(final Object context, final Options options)
            throws IOException {
        Object operator = options.param(0);
        Object operand = options.param(1);
        Object res;
        String s = operator.toString();
        if (s.equals("==")) {
            res = context.equals(operand);

        } else if (s.equals("!==")) {
            res = !context.equals(operand);

        } else if (s.equals("<")) {
            res = (Integer) context < (Integer) operand;

        } else if (s.equals("<=")) {
            res = (Integer) context <= (Integer) operand;

        } else if (s.equals(">")) {
            res = (Integer) context > (Integer) operand;

        } else if (s.equals(">=")) {
            res = (Integer) context >= (Integer) operand;

        } else if (s.equals("&&")) {
            res = (Boolean) context && (Boolean) operand;

        } else if (s.equals("||")) {
            res = (Boolean) context || (Boolean) operand;

        } else {
            res = options.fn(context);

        }
        if (options.isFalsy(res)) {
            return options.inverse(res);
        } else {
            return options.fn(res);
        }
    }

}
