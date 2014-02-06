
package org.xbib.elasticsearch.tools;

import org.xbib.elasticsearch.ResourceSink;
import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.elasticsearch.support.client.bulk.BulkClient;
import org.xbib.elasticsearch.support.client.ingest.IngestClient;
import org.xbib.elasticsearch.support.client.ingest.MockIngestClient;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineRequest;
import org.xbib.pipeline.element.PipelineElement;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.tools.QueueConverter;
import org.xbib.util.DateUtil;
import org.xbib.util.DurationFormatUtil;
import org.xbib.util.FormatUtil;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.text.NumberFormat;
import java.util.Date;

public abstract class QueueFeeder<T, R extends PipelineRequest, P extends Pipeline<T,R>, E extends PipelineElement>
    extends QueueConverter<T,R,P,E> {

    private final Logger logger = LoggerFactory.getLogger(QueueFeeder.class.getSimpleName());

    protected static Ingest output;

    protected static ResourceSink<ResourceContext, Resource> sink;

    @Override
    public QueueFeeder<T,R,P,E> reader(Reader reader) {
        super.reader(reader);
        return this;
    }

    @Override
    public QueueFeeder<T,R,P,E> writer(Writer writer) {
        super.writer(writer);
        return this;
    }

    protected Ingest createIngest() {
        return settings.getAsBoolean("mock", false) ?
                new MockIngestClient() :
                "ingest".equals(settings.get("client")) ?
                        new IngestClient() :
                        new BulkClient();
    }

    @Override
    protected QueueFeeder<T,R,P,E> prepare() throws IOException {
        super.prepare();
        // prepare output
        URI esURI = URI.create(settings.get("elasticsearch"));
        String index = settings.get("index");
        String type = settings.get("type");
        Integer shards = settings.getAsInt("shards", 1);
        Integer replica = settings.getAsInt("replica", 0);
        Integer maxbulkactions = settings.getAsInt("maxbulkactions", 100);
        Integer maxconcurrentbulkrequests = settings.getAsInt("maxconcurrentbulkrequests",
                Runtime.getRuntime().availableProcessors());
        output = createIngest();
        prepare(output);
        output.maxActionsPerBulkRequest(maxbulkactions)
                .maxConcurrentBulkRequests(maxconcurrentbulkrequests)
                .newClient(esURI);
        output.waitForCluster();
        output.setIndex(index)
                .setType(type)
                .dateDetection(false)
                .shards(shards)
                .replica(replica)
                .newIndex();
        sink = new ResourceSink(output);
        return this;
    }

    @Override
    protected QueueFeeder<T,R,P,E> cleanup() {
        super.cleanup();
        if (output != null) {
            logger.info("shutdown in progress");
            output.shutdown();
        }
        logger.info("done");
        return this;
    }

    @Override
    protected void writeMetrics(Writer writer) throws Exception {
        // TODO write output metrics, index output metrics

        long docs = 0L;
        long bytes = 0L;
        for (Pipeline p : executor.getPipelines()) {
            docs += p.count();
            bytes += p.size();
        }

        long elapsed = executor.metric().elapsed() / 1000000;
        double dps = docs * 1000 / elapsed;
        double avg = bytes / (docs + 1); // avoid div by zero
        double mbps = (bytes * 1000 / elapsed) / (1024 * 1024) ;
        NumberFormat formatter = NumberFormat.getNumberInstance();
        logger.info("Indexing complete. {} inputs, {} docs, {} = {} ms, {} = {} bytes, {} = {} avg size, {} dps, {} MB/s",
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
            logger.info("pipeline {}, started {}, ended {}, took {}",
                    p,
                    DateUtil.formatDateISO(new Date(p.startedAt())),
                    DateUtil.formatDateISO(new Date(p.stoppedAt())),
                    DurationFormatUtil.formatDurationWords(p.took(), true, true));
        }

        if (writer != null) {
            String metrics = String.format("Indexing complete. %d inputs, %d docs, %s = %d ms, %d = %s bytes, %s = %s avg size, %s dps, %s MB/s",
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

    /**
     * Here, the feeder can prepare the ingester.
     * @param output the ingester for output
     * @return this feeder
     */
    protected abstract QueueFeeder<T,R,P,E> prepare(Ingest output);

}
