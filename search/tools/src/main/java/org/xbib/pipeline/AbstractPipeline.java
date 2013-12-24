
package org.xbib.pipeline;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.metrics.MeterMetric;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Basic pipeline
 *
 * @param <R> the pipeline request type
 */
public abstract class AbstractPipeline<R extends PipelineRequest>
        implements Pipeline<Long,R> {

    private final static Logger logger = LoggerFactory.getLogger(AbstractPipeline.class.getSimpleName());

    private PipelineExecutor<Pipeline<Long,R>> executor;

    private Map<String,PipelineListener<Long,R>> listeners = new LinkedHashMap<String,PipelineListener<Long,R>>();

    private Long count;

    private MeterMetric metric;

    private Throwable lastException;

    @Override
    public Pipeline<Long, R> executor(PipelineExecutor<Pipeline<Long, R>> executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public Pipeline<Long,R> addLast(String name, PipelineListener<Long,R> listener) {
        this.listeners.put(name,listener);
        return this;
    }

    @Override
    public Long call() throws Exception {
        count = 0L;
        try {
            metric = new MeterMetric(5L, TimeUnit.SECONDS);
            Iterator<R> it = this;
            while (it.hasNext()) {
                R r = it.next();
                logger.info("got request {}", r);
                count++;
                metric.mark(r.size());
                for (PipelineListener<Long,R> listener : listeners.values()) {
                    listener.listen(executor, this, r);
                }
            }
            close();
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            this.lastException = t;
            throw new IllegalStateException(t);
        } finally {
            metric.stop();
        }
        return count;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove not supported");
    }

    @Override
    public Long count() {
        return count;
    }

    @Override
    public Long size() {
        return metric.count();
    }

    @Override
    public Long startedAt() {
        return metric.started();
    }

    @Override
    public Long stoppedAt() {
        return metric.stopped();
    }

    @Override
    public Long took() {
        return metric.elapsed();
    }

    public Throwable lastException() {
        return lastException;
    }

}
