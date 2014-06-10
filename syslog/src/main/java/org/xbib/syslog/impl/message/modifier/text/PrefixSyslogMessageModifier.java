package org.xbib.syslog.impl.message.modifier.text;

import org.xbib.syslog.SyslogIF;
import org.xbib.syslog.SyslogMessageModifierIF;

/**
 * PrefixSyslogMessageModifier is an implementation of SyslogMessageModifierIF
 * that provides support for adding static text to the beginning of a Syslog message.
 */
public class PrefixSyslogMessageModifier implements SyslogMessageModifierIF {

    protected String prefix = null;
    protected String delimiter = " ";

    public PrefixSyslogMessageModifier() {
        //
    }

    public PrefixSyslogMessageModifier(String prefix) {
        this.prefix = prefix;
    }

    public PrefixSyslogMessageModifier(String prefix, String delimiter) {
        this.prefix = prefix;
        if (delimiter != null) {
            this.delimiter = delimiter;
        }
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String modify(SyslogIF syslog, int facility, int level, String message) {
        if (this.prefix == null || "".equals(this.prefix.trim())) {
            return message;
        }

        return this.prefix + this.delimiter + message;
    }

    public boolean verify(String message) {
        // NO-OP

        return true;
    }
}
