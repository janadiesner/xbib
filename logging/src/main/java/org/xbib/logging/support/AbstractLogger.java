/**
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.logging.support;

import org.xbib.logging.Logger;

public abstract class AbstractLogger implements Logger {

    private final String prefix;

    protected AbstractLogger(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public void trace(String msg, Object... params) {
        if (isTraceEnabled()) {
            internalTrace(LoggerMessageFormat.format(prefix, msg, params));
        }
    }

    protected abstract void internalTrace(String msg);

    @Override
    public void trace(String msg, Throwable cause, Object... params) {
        if (isTraceEnabled()) {
            internalTrace(LoggerMessageFormat.format(prefix, msg, params), cause);
        }
    }

    protected abstract void internalTrace(String msg, Throwable cause);

    @Override
    public void debug(String msg, Object... params) {
        if (isDebugEnabled()) {
            internalDebug(LoggerMessageFormat.format(prefix, msg, params));
        }
    }

    protected abstract void internalDebug(String msg);

    @Override
    public void debug(String msg, Throwable cause, Object... params) {
        if (isDebugEnabled()) {
            internalDebug(LoggerMessageFormat.format(prefix, msg, params), cause);
        }
    }

    protected abstract void internalDebug(String msg, Throwable cause);

    @Override
    public void info(String msg, Object... params) {
        if (isInfoEnabled()) {
            internalInfo(LoggerMessageFormat.format(prefix, msg, params));
        }
    }

    protected abstract void internalInfo(String msg);

    @Override
    public void info(String msg, Throwable cause, Object... params) {
        if (isInfoEnabled()) {
            internalInfo(LoggerMessageFormat.format(prefix, msg, params), cause);
        }
    }

    protected abstract void internalInfo(String msg, Throwable cause);

    @Override
    public void warn(String msg, Object... params) {
        if (isWarnEnabled()) {
            internalWarn(LoggerMessageFormat.format(prefix, msg, params));
        }
    }

    protected abstract void internalWarn(String msg);

    @Override
    public void warn(String msg, Throwable cause, Object... params) {
        if (isWarnEnabled()) {
            internalWarn(LoggerMessageFormat.format(prefix, msg, params), cause);
        }
    }

    protected abstract void internalWarn(String msg, Throwable cause);

    @Override
    public void error(String msg, Object... params) {
        if (isErrorEnabled()) {
            internalError(LoggerMessageFormat.format(prefix, msg, params));
        }
    }

    protected abstract void internalError(String msg);

    @Override
    public void error(String msg, Throwable cause, Object... params) {
        if (isErrorEnabled()) {
            internalError(LoggerMessageFormat.format(prefix, msg, params), cause);
        }
    }

    protected abstract void internalError(String msg, Throwable cause);
}
