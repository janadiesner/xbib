/*
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
package org.xbib.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.unit.TimeValue;
import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.elasticsearch.support.client.transport.BulkTransportClient;
import org.xbib.elasticsearch.support.client.ingest.IngestTransportClient;
import org.xbib.elasticsearch.support.client.mock.MockTransportClient;
import org.xbib.metric.MeterMetric;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineRequest;
import org.xbib.util.DurationFormatUtil;
import org.xbib.util.FormatUtil;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.NumberFormat;

public abstract class Feeder<T, R extends PipelineRequest, P extends Pipeline<T, R>>
        extends Converter<T, R, P> {

    private final static Logger logger = LogManager.getLogger(Feeder.class.getSimpleName());

    protected static Ingest ingest;

    @Override
    public Feeder<T, R, P> reader(Reader reader) {
        super.reader(reader);
        return this;
    }

    @Override
    public Feeder<T, R, P> writer(Writer writer) {
        super.writer(writer);
        return this;
    }

    protected String getIndex() {
        return settings.get("index");
    }

    protected String getType() {
        return settings.get("type");
    }

    protected Ingest createIngest() {
        return settings.getAsBoolean("mock", false) ?
                new MockTransportClient() :
                "ingest".equals(settings.get("client")) ? new IngestTransportClient() :
                        new BulkTransportClient();
    }

    @Override
    protected Feeder<T, R, P> prepare() throws IOException {
        super.prepare();
        if (ingest == null) {
            Integer maxbulkactions = settings.getAsInt("maxbulkactions", 1000);
            Integer maxconcurrentbulkrequests = settings.getAsInt("maxconcurrentbulkrequests",
                    Runtime.getRuntime().availableProcessors());
            ingest = createIngest();
            ingest.maxActionsPerBulkRequest(maxbulkactions)
                    .maxConcurrentBulkRequests(maxconcurrentbulkrequests);
        }
        createIndex(getIndex());
        return this;
    }

    @Override
    protected Feeder<T, R, P> cleanup() throws IOException {
        super.cleanup();
        if (ingest != null) {
            try {
                logger.info("flush");
                ingest.flushIngest();
                logger.info("waiting for all responses");
                ingest.waitForResponses(TimeValue.timeValueSeconds(120));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error(e.getMessage(), e);
            }
            logger.info("shutdown");
            ingest.shutdown();
        }
        logger.info("done with run");
        return this;
    }

    @Override
    protected void writeMetrics(MeterMetric metric, Writer writer) throws Exception {
        long docs = metric.count();
        double mean = metric.meanRate();
        double oneminute = metric.oneMinuteRate();
        double fiveminute = metric.fiveMinuteRate();
        double fifteenminute = metric.fifteenMinuteRate();
        long bytes = ingest.getMetric().getTotalIngestSizeInBytes().count();
        long elapsed = metric.elapsed() / 1000000;
        String elapsedhuman = DurationFormatUtil.formatDurationWords(elapsed, true, true);
        double avg = bytes / (docs + 1); // avoid div by zero
        double mbps = (bytes * 1000.0 / elapsed) / (1024.0 * 1024.0);
        NumberFormat formatter = NumberFormat.getNumberInstance();
        logger.info("indexing metrics: elapsed {}, {} docs, {} bytes, {} avgsize, {} MB/s, {} ({} {} {})",
                elapsedhuman,
                docs,
                FormatUtil.convertFileSize(bytes),
                FormatUtil.convertFileSize(avg),
                formatter.format(mbps),
                mean,
                oneminute,
                fiveminute,
                fifteenminute
        );

        if (writer != null) {
            String metrics = String.format("indexing metrics: elapsed %s, %d docs, %s bytes, %s avgsize, %s MB/s, %f (%f %f %f)",
                    elapsedhuman,
                    docs,
                    FormatUtil.convertFileSize(bytes),
                    FormatUtil.convertFileSize(avg),
                    formatter.format(mbps),
                    mean,
                    oneminute,
                    fiveminute,
                    fifteenminute
            );
            writer.append(metrics);
        }
    }

    protected Feeder createIndex(String index) throws IOException {
        ingest.newClient(ImmutableSettings.settingsBuilder()
                .put("cluster.name", settings.get("elasticsearch.cluster"))
                .put("host", settings.get("elasticsearch.host"))
                .put("port", settings.getAsInt("elasticsearch.port", 9300))
                .put("sniff", settings.getAsBoolean("elasticsearch.sniff", false))
                .build());
        ingest.waitForCluster(ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(30));
        try {
            beforeIndexCreation(ingest);
            ingest.newIndex(index);
        } catch (Exception e) {
            if (!settings.getAsBoolean("ignoreindexcreationerror", false)) {
                throw e;
            } else {
                logger.warn("index creation error, but configured to ignore");
            }
        }
        return this;
    }

    protected Feeder beforeIndexCreation(Ingest output) throws IOException {
        return this;
    }
}
