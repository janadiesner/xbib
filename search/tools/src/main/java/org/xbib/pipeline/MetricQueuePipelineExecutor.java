
package org.xbib.pipeline;

import org.xbib.metrics.MeterMetric;
import org.xbib.pipeline.element.PipelineElement;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MetricQueuePipelineExecutor<T, R extends PipelineRequest, P extends Pipeline<T,R>, E extends PipelineElement>
        extends QueuePipelineExecutor<T,R,P,E> {

    protected MeterMetric metric;

    @Override
    public MetricQueuePipelineExecutor<T,R,P,E> concurrency(int concurrency) {
        super.concurrency(concurrency);
        return this;
    }

    @Override
    public MetricQueuePipelineExecutor<T,R,P,E> provider(PipelineProvider<P> provider) {
        super.provider(provider);
        return this;
    }

    @Override
    public MetricQueuePipelineExecutor<T,R,P,E> sink(PipelineSink sink) {
        super.sink(sink);
        return this;
    }

    @Override
    public MetricQueuePipelineExecutor<T,R,P,E> prepare() throws IOException {
        super.prepare();
        return this;
    }

    @Override
    public MetricQueuePipelineExecutor<T,R,P,E> execute() throws IOException {
        metric = new MeterMetric(5L, TimeUnit.SECONDS);
        super.execute();
        return this;
    }

    @Override
    public MetricQueuePipelineExecutor<T,R,P,E> execute(P pipeline) throws InterruptedException, ExecutionException {
        super.execute(pipeline);
        return this;
    }

    @Override
    public MetricQueuePipelineExecutor<T,R,P,E> waitFor() throws InterruptedException, ExecutionException {
        super.waitFor();
        metric.stop();
        return this;
    }

    public MeterMetric metric() {
        return metric;
    }
}
