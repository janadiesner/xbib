
package org.xbib.tools;

import org.xbib.elasticsearch.tools.QueueFeeder;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineExecutor;
import org.xbib.pipeline.PipelineListener;
import org.xbib.pipeline.PipelineRequest;
import org.xbib.pipeline.element.PipelineElement;
import org.xbib.util.ExceptionFormatter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Maps.newHashMap;

public abstract class QueueConverterPipeline<T, R extends PipelineRequest, P extends Pipeline<T,R>, E extends PipelineElement>
        implements Pipeline<Boolean, R> {

    private final Logger logger;

    private QueueConverter<T, R, P, E> converter;

    private Long t0;

    private Long t1;

    private Long count;

    private Long volumeCount;

    private Map<String, PipelineListener<Boolean,R>> listeners;

    private R request;

    private E element;

    public QueueConverterPipeline(QueueConverter<T, R, P, E> converter, int num) {
        this.converter = converter;
        this.listeners = newHashMap();
        this.logger = LoggerFactory.getLogger(QueueConverterPipeline.class.getSimpleName() + "-pipeline-" + num);
    }

    @Override
    public Pipeline<Boolean, R> addLast(String name, PipelineListener<Boolean, R> listener) {
        listeners.put(name, listener);
        return this;
    }

    @Override
    public Pipeline<Boolean, R> executor(PipelineExecutor<Pipeline<Boolean, R>> executor) {
        // unused, we use the converter
        return this;
    }

    @Override
    public Long count() {
        return count;
    }

    @Override
    public Long size() {
        return volumeCount;
    }

    @Override
    public Long startedAt() {
        return t0;
    }

    @Override
    public Long stoppedAt() {
        return t1;
    }

    @Override
    public Long took() {
        return t0 != null && t1 != null ? t1 - t0 : null;
    }

    @Override
    public Boolean call() throws Exception {
        logger.info("pipeline starting");
        try {
            t0 = System.currentTimeMillis();
            while (hasNext()) {
                request = next();
                process(request);
                for (PipelineListener<Boolean,R> listener : listeners.values()) {
                    listener.listen((PipelineExecutor<Pipeline<Boolean,R>>) converter, this, request);
                }
                count++;
            }
        } catch (Throwable e) {
            logger.error("exception while processing {}, exiting", request);
            logger.error(ExceptionFormatter.format(e));
        } finally {
            converter.countDown();
            t1 = System.currentTimeMillis();
        }
        logger.info("pipeline terminating");
        return true;
    }


    @Override
    public boolean hasNext() {
        try {
            element = converter.queue().poll(60, TimeUnit.SECONDS); // TODO make poll configurable
            if (element == null) {
                logger.warn("queue is empty?");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            element = null;
            logger.error("pipeline processing interrupted, queue inactive", e);
        }
        return element != null && element.get() != null;
    }

    @Override
    public abstract R next();

    @Override
    public void close() throws IOException {
        logger.info("closing");
    }

    protected E getElement() {
        return element;
    }

    protected abstract void process(R request) throws IOException;
}
