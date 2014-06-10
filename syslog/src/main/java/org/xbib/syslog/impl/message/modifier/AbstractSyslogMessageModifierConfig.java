package org.xbib.syslog.impl.message.modifier;

import org.xbib.syslog.SyslogCharSetIF;
import org.xbib.syslog.SyslogConstants;
import org.xbib.syslog.SyslogMessageModifierConfigIF;

/**
 * AbstractSyslogMessageModifierConfig provides a base abstract implementation of the
 * SyslogMessageModifierConfigIF.
 */
public abstract class AbstractSyslogMessageModifierConfig implements SyslogMessageModifierConfigIF, SyslogCharSetIF {

    protected String prefix = SYSLOG_MESSAGE_MODIFIER_PREFIX_DEFAULT;

    protected String suffix = SYSLOG_MESSAGE_MODIFIER_SUFFIX_DEFAULT;

    protected String charSet = SyslogConstants.CHAR_SET_DEFAULT;

    public String getPrefix() {
        return this.prefix;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public void setPrefix(String prefix) {
        if (prefix == null) {
            this.prefix = "";

        } else {
            this.prefix = prefix;
        }
    }

    public void setSuffix(String suffix) {
        if (suffix == null) {
            this.suffix = "";

        } else {
            this.suffix = suffix;
        }
    }

    public String getCharSet() {
        return charSet;
    }

    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }
}
