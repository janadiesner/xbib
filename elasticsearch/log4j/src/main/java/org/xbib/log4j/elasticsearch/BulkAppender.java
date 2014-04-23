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
package org.xbib.log4j.elasticsearch;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;

import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.elasticsearch.support.client.Feeder;
import org.xbib.elasticsearch.support.client.bulk.BulkTransportClient;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

/**
 */
public class BulkAppender extends AppenderSkeleton {

    private Feeder feeder;

    private String index;

    private String type;

    private final static AtomicLong id = new AtomicLong();

    public BulkAppender() {
    }

    public void setFeeder(Feeder feeder) {
        this.feeder = feeder;
    }

    /**
     * Returns the current value of the <b>Target</b> property. The
     * default value of the option is "System.out".
     * <p/>
     * See also {@link #setFeeder}.
     */
    public Feeder getFeeder() {
        return feeder;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getIndex() {
        return index;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    /**
     * Prepares the appender for use.
     */
    @Override
    public void activateOptions() {
        if (feeder == null) {
            this.feeder = new BulkTransportClient().newClient();
        }

        super.activateOptions();
    }

    @Override
    protected void append(LoggingEvent loggingEvent) {
        try {
            XContentBuilder builder = jsonBuilder();
            builder.startObject().startObject("event")
                    .field("logger", loggingEvent.getLoggerName())
                    .field("timestamp", loggingEvent.getTimeStamp())
                    .field("level", loggingEvent.getLevel())
                    .field("thread", loggingEvent.getThreadName())
                    .field("message", loggingEvent.getRenderedMessage());

            String ndc = loggingEvent.getNDC();
            if (ndc != null) {
                builder.field("ndc", loggingEvent.getNDC());
            }

            builder.field("throwable", loggingEvent.getThrowableStrRep());

            if (loggingEvent.locationInformationExists()) {
                LocationInfo locationInfo = loggingEvent.getLocationInformation();
                builder.startObject("locationInfo")
                        .field("class", locationInfo.getClassName())
                        .field("method", locationInfo.getMethodName())
                        .field("file", locationInfo.getFileName())
                        .field("line", locationInfo.getLineNumber())
                        .endObject();
            }

            Set keySet = loggingEvent.getPropertyKeySet();
            if (!keySet.isEmpty()) {
                builder.startObject("properties");
                for (Object key : keySet) {
                    String k = key.toString();
                    builder.field(k, loggingEvent.getMDC(k));
                }
                builder.endObject();
            }

            builder.endObject().endObject();

            if (feeder != null && index != null && type != null) {
                feeder.index(index, type, Long.toString(id.incrementAndGet()),
                    builder.string());
            }

        } catch (IOException e) {
            // ignore
        }
    }

    @Override
    public void close() {
        feeder.client().close();
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
