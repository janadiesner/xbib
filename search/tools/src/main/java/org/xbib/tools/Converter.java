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

import org.xbib.common.settings.Settings;
import org.xbib.io.file.Finder;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.pipeline.AbstractPipeline;
import org.xbib.pipeline.MetricPipelineExecutor;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.PipelineRequest;
import org.xbib.pipeline.element.CounterElement;
import org.xbib.util.DurationFormatUtil;
import org.xbib.util.FormatUtil;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.text.NumberFormat;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

import static org.xbib.common.settings.ImmutableSettings.settingsBuilder;

public abstract class Converter<T, R extends PipelineRequest, P extends Pipeline<T,R>>
        extends AbstractPipeline<CounterElement> {

    private final static Logger logger = LoggerFactory.getLogger(Converter.class.getSimpleName());

    private final static CounterElement counter = new CounterElement().set(new AtomicLong(0L));

    protected Reader reader;

    protected Writer writer;

    protected static Settings settings;

    protected static Queue<URI> input;

    protected MetricPipelineExecutor<T,R,P> executor;

    private boolean done = false;

    public Converter<T,R,P> reader(Reader reader) {
        this.reader = reader;
        settings = settingsBuilder().loadFromReader(reader).build();
        return this;
    }

    public Converter<T,R,P> writer(Writer writer) {
        this.writer = writer;
        return this;
    }

    public Converter<T,R,P> run() throws Exception {
        try {
            logger.info("preparing");
            prepare();
            logger.info("executing");
            executor = new MetricPipelineExecutor<T,R,P>()
                    .concurrency(settings.getAsInt("concurrency", 1))
                    .provider(pipelineProvider())
                    .prepare()
                    .execute()
                    .waitFor();
            logger.info("execution completed");
        } finally {
            cleanup();
            if (executor != null) {
                executor.shutdown();
                writeMetrics(writer);
            }
        }
        return this;
    }

    protected Converter<T,R,P> prepare() {
        try {
            input = new Finder(settings.get("pattern"))
                    .find(settings.get("path"))
                    .pathSorted(settings.getAsBoolean("isPathSorted", false))
                    .chronologicallySorted(settings.getAsBoolean("isChronologicallySorted", false))
                    .getURIs();
            logger.info("input = {}", input);
        } catch (IOException e) {
            logger.error("no input found", e);
        }
        return this;
    }

    protected Converter<T,R,P> cleanup() {
        return this;
    }

    @Override
    public void close() throws IOException {
        logger.info("pipeline close (no op)");
    }

    @Override
    public boolean hasNext() {
        return !done && !input.isEmpty();
    }

    @Override
    public CounterElement next() {
        URI uri = input.poll();
        done = uri == null;
        if (done) {
            logger.info("done is true");
            return counter;
        }
        try {
            process(uri);
            counter.get().incrementAndGet();
        } catch (Exception ex) {
            logger.error("error while getting next input: " + ex.getMessage(), ex);
        }
        return counter;
    }

    protected void writeMetrics(Writer writer) throws Exception {
        long docs = executor.getTotalCount();
        long bytes = executor.getTotalSize();
        long elapsed = executor.metric().elapsed() / 1000000;
        double dps = docs * 1000 / elapsed;
        double avg = bytes / (docs + 1); // avoid div by zero
        double mbps = (bytes * 1000 / elapsed) / (1024 * 1024) ;
        NumberFormat formatter = NumberFormat.getNumberInstance();
        logger.info("Converter complete. {} inputs, {} docs, {} = {} ms, {} = {} bytes, {} = {} avg size, {} dps, {} MB/s",
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
        if (writer != null) {
            String metrics = String.format("Converter complete. %d inputs, %d docs, %s = %d ms, %d = %s bytes, %s = %s avg size, %s dps, %s MB/s",
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

    protected abstract PipelineProvider<P> pipelineProvider();

    protected abstract void process(URI uri) throws Exception;

}
