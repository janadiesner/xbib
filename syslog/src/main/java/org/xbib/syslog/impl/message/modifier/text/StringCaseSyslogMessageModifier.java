package org.xbib.syslog.impl.message.modifier.text;

import org.xbib.syslog.Syslogger;
import org.xbib.syslog.SyslogMessageModifierIF;
import org.xbib.syslog.SyslogRuntimeException;

/**
 * StringCaseSyslogMessageModifier is an implementation of SyslogMessageModifierIF
 * that provides support for shifting a Syslog message to all upper case or all
 * lower case.
 */
public class StringCaseSyslogMessageModifier implements SyslogMessageModifierIF {

    public static final byte LOWER_CASE = 0;
    public static final byte UPPER_CASE = 1;

    public static final StringCaseSyslogMessageModifier LOWER = new StringCaseSyslogMessageModifier(LOWER_CASE);
    public static final StringCaseSyslogMessageModifier UPPER = new StringCaseSyslogMessageModifier(UPPER_CASE);

    protected byte stringCase = LOWER_CASE;

    public StringCaseSyslogMessageModifier(byte stringCase) {
        this.stringCase = stringCase;

        if (stringCase < LOWER_CASE || stringCase > UPPER_CASE) {
            throw new SyslogRuntimeException("stringCase must be LOWER_CASE (0) or UPPER_CASE (1)");
        }
    }

    public String modify(Syslogger syslog, int facility, int level, String message) {
        String _message = message;

        if (message != null) {
            if (this.stringCase == LOWER_CASE) {
                _message = _message.toLowerCase();

            } else if (this.stringCase == UPPER_CASE) {
                _message = _message.toUpperCase();
            }
        }

        return _message;
    }

    public boolean verify(String message) {
        // NO-OP

        return true;
    }
}
