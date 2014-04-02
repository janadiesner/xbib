
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
        if ((checkCondition(options.param(0), options.param(1), options.param(2)))) {
            return options.inverse();
        } else {
            return options.fn();
        }
    }

    private boolean checkCondition(Object v1, Object operator, Object v2) {
        switch (operator.toString()) {
            case "==":
                return v1.equals(v2);
            case "!==":
                return !v1.equals(v2);
            case "<":
                return (Long)v1 < (Long)v2;
            case "<=":
                return (Long)v1 <= (Long)v2;
            case ">":
                return (Long)v1 > (Long)v2;
            case ">=":
                return (Long)v1 >= (Long)v2;
            case "&&":
                return (Boolean)v1 && (Boolean)v2;
            case "||":
                return (Boolean)v1 || (Boolean)v2;
            default:
                return false;
        }
    }
}
