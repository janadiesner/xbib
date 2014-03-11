
package org.xbib.tools;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.metric.MeterMetric;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineRequestListener;
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

    private MeterMetric metric;

    private R request;

    private E element;

    private Map<String, PipelineRequestListener<Boolean,R>> listeners;

    public QueueConverterPipeline(QueueConverter<T, R, P, E> converter, int num) {
        this.converter = converter;
        this.listeners = newHashMap();
        this.logger = LoggerFactory.getLogger(QueueConverterPipeline.class.getSimpleName() + "-pipeline-" + num);
    }

    public Pipeline<Boolean, R> add(String name, PipelineRequestListener<Boolean, R> listener) {
        listeners.put(name, listener);
        return this;
    }

    @Override
    public MeterMetric getMetric() {
        return metric;
    }

    @Override
    public Boolean call() throws Exception {
        logger.info("pipeline starting");
        try {
            metric = new MeterMetric(5L, TimeUnit.SECONDS);
            while (hasNext()) {
                request = next();
                process(request);
                for (PipelineRequestListener<Boolean,R> listener : listeners.values()) {
                    listener.newRequest(this, request);
                }
                metric.mark();
            }
        } catch (Throwable e) {
            logger.error("exception while processing {}, exiting", request);
            logger.error(ExceptionFormatter.format(e));
        } finally {
            converter.countDown();
            metric.stop();
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
