package org.xbib.syslog.impl.message.modifier.text;

import org.xbib.syslog.SyslogIF;
import org.xbib.syslog.SyslogMessageModifierIF;

/**
 * SuffixSyslogMessageModifier is an implementation of SyslogMessageModifierIF
 * that provides support for adding static text to the end of a Syslog message.
 */
public class SuffixSyslogMessageModifier implements SyslogMessageModifierIF {

    protected String suffix = null;
    protected String delimiter = " ";

    public SuffixSyslogMessageModifier() {
        //
    }

    public SuffixSyslogMessageModifier(String suffix) {
        this.suffix = suffix;
    }

    public SuffixSyslogMessageModifier(String suffix, String delimiter) {
        this.suffix = suffix;
        if (delimiter != null) {
            this.delimiter = delimiter;
        }
    }

    public String getSuffix() {
        return this.suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String modify(SyslogIF syslog, int facility, int level, String message) {
        if (this.suffix == null || "".equals(this.suffix.trim())) {
            return message;
        }

        return message + this.delimiter + this.suffix;
    }

    public boolean verify(String message) {
        return true;
    }
}
