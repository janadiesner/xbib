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

import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.unit.TimeValue;
import org.xbib.elasticsearch.rdf.ResourceSink;
import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.elasticsearch.support.client.bulk.BulkTransportClient;
import org.xbib.elasticsearch.support.client.ingest.IngestTransportClient;
import org.xbib.elasticsearch.support.client.ingest.MockIngestTransportClient;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineRequest;
import org.xbib.util.DateUtil;
import org.xbib.util.DurationFormatUtil;
import org.xbib.util.FormatUtil;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.text.NumberFormat;

public abstract class Feeder<T, R extends PipelineRequest, P extends Pipeline<T,R>>
        extends Converter<T,R,P> {

    private final static Logger logger = LoggerFactory.getLogger(Feeder.class.getSimpleName());

    protected static Ingest output;

    protected static ResourceSink sink;

    @Override
    public Feeder<T,R,P> reader(Reader reader) {
        super.reader(reader);
        return this;
    }

    @Override
    public Feeder<T,R,P> writer(Writer writer) {
        super.writer(writer);
        return this;
    }

    protected Ingest createIngest() {
        return settings.getAsBoolean("mock", false) ?
                new MockIngestTransportClient() :
                "ingest".equals(settings.get("client")) ?
                        new IngestTransportClient() :
                        new BulkTransportClient();
    }

    @Override
    protected Feeder<T,R,P> prepare() throws IOException {
        super.prepare();
        URI esURI = URI.create(settings.get("elasticsearch"));
        String index = settings.get("index");
        String type = settings.get("type");
        Integer shards = settings.getAsInt("shards", 1);
        Integer replica = settings.getAsInt("replica", 0);
        Integer maxbulkactions = settings.getAsInt("maxbulkactions", 100);
        Integer maxconcurrentbulkrequests = settings.getAsInt("maxconcurrentbulkrequests",
                Runtime.getRuntime().availableProcessors());
        String maxtimewait = settings.get("maxtimewait", "60s");
        output = createIngest();
        output.maxActionsPerBulkRequest(maxbulkactions)
                .maxConcurrentBulkRequests(maxconcurrentbulkrequests)
                .maxRequestWait(TimeValue.parseTimeValue(maxtimewait, TimeValue.timeValueSeconds(60)))
                .newClient(esURI);
        output.waitForCluster(ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(30));
        beforeIndexCreation(output);
        output.setIndex(index)
                .setType(type)
                .shards(shards)
                .replica(replica)
                .newIndex()
                .startBulk();
        afterIndexCreation(output);
        sink = new ResourceSink(output);
        return this;
    }

    @Override
    protected Feeder<T,R,P> cleanup() {
        super.cleanup();
        if (output != null) {
            logger.info("shutdown");
            try {
                output.stopBulk();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } finally {
                output.shutdown();
            }
        }
        logger.info("done with run");
        return this;
    }

    @Override
    protected void writeMetrics(Writer writer) throws Exception {
        // TODO write output metrics, index output metrics
        long docs = executor.metric().count();
        long bytes = 0L;
        long elapsed = executor.metric().elapsed() / 1000000;
        double dps = docs * 1000 / elapsed;
        double avg = bytes / (docs + 1); // avoid div by zero
        double mbps = (bytes * 1000 / elapsed) / (1024 * 1024) ;
        NumberFormat formatter = NumberFormat.getNumberInstance();
        logger.info("Indexing complete. {} inputs, {} docs, {} = {} ms, {} = {} bytes, {} = {} avg getSize, {} dps, {} MB/s",
                input.size(),
                docs,
                DurationFormatUtil.formatDurationWords(elapsed, true, true),
                elapsed,
                bytes,
                FormatUtil.convertFileSize(bytes),
                FormatUtil.convertFileSize(avg),
                formatter.format(avg),
                formatter.format(dps),
                formatter.format(mbps));

        for (Pipeline p : executor.getPipelines()) {
            logger.info("pipeline {}, started {}, ended {}, took {}, count = {}",
                    p,
                    DateUtil.formatDateISO(p.getMetric().startedAt()),
                    DateUtil.formatDateISO(p.getMetric().stoppedAt()),
                    DurationFormatUtil.formatDurationWords(p.getMetric().elapsed()/1000000, true, true),
                    p.getMetric().count());
        }

        if (writer != null) {
            String metrics = String.format("Indexing complete. %d inputs, %d docs, %s = %d ms, %d = %s bytes, %s = %s avg getSize, %s dps, %s MB/s",
                    input.size(),
                    docs,
                    DurationFormatUtil.formatDurationWords(elapsed, true, true),
                    elapsed,
                    bytes,
                    FormatUtil.convertFileSize(bytes),
                    FormatUtil.convertFileSize(avg),
                    formatter.format(avg),
                    formatter.format(dps),
                    formatter.format(mbps));
            writer.append(metrics);
        }
    }

    protected Feeder beforeIndexCreation(Ingest output) throws IOException {
        return this;
    }

    protected Feeder afterIndexCreation(Ingest output) throws IOException {
        return this;
    }

}
