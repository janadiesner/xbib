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
package org.xbib.logging.log4j;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.OutputStreamManager;
import org.apache.logging.log4j.core.layout.PatternLayout;

import org.xbib.logging.Loggers;

/**
 * ConsoleAppender appends log events to <code>System.out</code> or
 * <code>System.err</code> using a layout specified by the user. The
 * default target is <code>System.out</code>.
 */
@Plugin(name = "ExtConsole", category = "xbib", elementType = "appender", printObject = true)
public class ExtConsoleAppender extends AbstractOutputStreamAppender<OutputStreamManager> {

    public enum Target {
        /** Standard output. */
        SYSTEM_OUT,
        /** Standard error output. */
        SYSTEM_ERR
    }

    private static ConsoleManagerFactory factory = new ConsoleManagerFactory();

    /**
     * Constructs an unconfigured appender.
     */
    public ExtConsoleAppender(final String name, final Layout layout, final Filter filter,
                              final OutputStreamManager manager, final boolean ignoreExceptions) {
        super(name, layout, filter, ignoreExceptions, true, manager);
    }

    @PluginFactory
    public static ExtConsoleAppender createAppender(
            @PluginElement("Layout") Layout layout,
            @PluginElement("Filters") final Filter filter,
            @PluginAttribute("target") final String t,
            @PluginAttribute("name") final String name,
            @PluginAttribute("follow") final String follow,
            @PluginAttribute("ignoreExceptions") final String ignore) {
        if (name == null) {
            LOGGER.error("No name provided for ConsoleAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createLayout(null, null, null, null, false, false, null, null);
        }
        final boolean isFollow = Boolean.parseBoolean(follow);
        final boolean ignoreExceptions = Boolean.parseBoolean(ignore);
        final Target target = t == null ? Target.SYSTEM_OUT : Target.valueOf(t);
        return new ExtConsoleAppender(name, layout, filter, getManager(isFollow, target, layout), ignoreExceptions);
    }

    private static OutputStreamManager getManager(final boolean follow, final Target target, final Layout layout) {
        final String type = target.name();
        final OutputStream os = getOutputStream(follow, target);
        return OutputStreamManager.getManager(target.name() + "." + follow, new FactoryData(os, type, layout), factory);
    }

    private static OutputStream getOutputStream(final boolean follow, final Target target) {
        final String enc = Charset.defaultCharset().name();
        PrintStream printStream = null;
        try {
            printStream = target == Target.SYSTEM_OUT ?
                    follow ? new PrintStream(new SystemOutStream(), true, enc) : System.out :
                    follow ? new PrintStream(new SystemErrStream(), true, enc) : System.err;
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException("Unsupported default encoding " + enc, ex);
        }
        return printStream;
    }

    /**
     * An implementation of OutputStream that redirects to the
     * current System.err.
     */
    private static class SystemErrStream extends OutputStream {
        public SystemErrStream() {
        }

        @Override
        public void close() {
        }

        @Override
        public void flush() {
            System.err.flush();
        }

        @Override
        public void write(final byte[] b) throws IOException {
            if (!Loggers.consoleLoggingEnabled()) {
                return;
            }
            System.err.write(b);
        }

        @Override
        public void write(final byte[] b, final int off, final int len)
                throws IOException {
            if (!Loggers.consoleLoggingEnabled()) {
                return;
            }
            System.err.write(b, off, len);
        }

        @Override
        public void write(final int b) throws IOException {
            if (!Loggers.consoleLoggingEnabled()) {
                return;
            }
            System.err.write(b);
        }
    }

    /**
     * An implementation of OutputStream that redirects to the
     * current System.out.
     */
    private static class SystemOutStream extends OutputStream {
        public SystemOutStream() {
        }

        @Override
        public void close() {
        }

        @Override
        public void flush() {
            System.out.flush();
        }

        @Override
        public void write(final byte[] b) throws IOException {
            if (!Loggers.consoleLoggingEnabled()) {
                return;
            }
            System.out.write(b);
        }

        @Override
        public void write(final byte[] b, final int off, final int len)
                throws IOException {
            if (!Loggers.consoleLoggingEnabled()) {
                return;
            }
            System.out.write(b, off, len);
        }

        @Override
        public void write(final int b) throws IOException {
            if (!Loggers.consoleLoggingEnabled()) {
                return;
            }
            System.out.write(b);
        }
    }

    /**
     * Data to pass to factory method.
     */
    private static class FactoryData {
        private final OutputStream os;
        private final String type;
        private final Layout layout;

        /**
         * Constructor.
         * @param os The OutputStream.
         * @param type The name of the target.
         * @param layout A Serializable layout
         */
        public FactoryData(final OutputStream os, final String type, final Layout layout) {
            this.os = os;
            this.type = type;
            this.layout = layout;
        }
    }

    /**
     * Factory to create the Appender.
     */
    private static class ConsoleManagerFactory implements ManagerFactory<OutputStreamManager, FactoryData> {

        /**
         * Create an OutputStreamManager.
         * @param name The name of the entity to manage.
         * @param data The data required to create the entity.
         * @return The OutputStreamManager
         */
        @Override
        public OutputStreamManager createManager(final String name, final FactoryData data) {
            return new MyOutputStreamManager(data.os, data.type, data.layout);
        }
    }

    private static class MyOutputStreamManager extends OutputStreamManager {

        protected MyOutputStreamManager(OutputStream os, String streamName, Layout<?> layout) {
            super(os, streamName, layout);
        }
    }
}
