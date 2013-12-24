package org.xbib.pipeline;

import org.xbib.metrics.MeterMetric;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MetricPipelineExecutor<T, R extends PipelineRequest, P extends Pipeline<T,R>>
        extends SimplePipelineExecutor<T,R,P> {

    protected MeterMetric metric;

    @Override
    public MetricPipelineExecutor<T,R,P> concurrency(int concurrency) {
        super.concurrency(concurrency);
        return this;
    }

    @Override
    public MetricPipelineExecutor<T,R,P> provider(PipelineProvider<P> provider) {
        super.provider(provider);
        return this;
    }

    @Override
    public MetricPipelineExecutor<T,R,P> sink(PipelineSink sink) {
        super.sink(sink);
        return this;
    }

    @Override
    public MetricPipelineExecutor<T,R,P> prepare() {
        super.prepare();
        return this;
    }

    @Override
    public MetricPipelineExecutor<T,R,P> execute() {
        metric = new MeterMetric(5L, TimeUnit.SECONDS);
        super.execute();
        return this;
    }

    @Override
    public MetricPipelineExecutor<T,R,P> execute(P pipeline) throws InterruptedException, ExecutionException {
        super.execute(pipeline);
        return this;
    }

    @Override
    public MetricPipelineExecutor<T,R,P> waitFor() throws InterruptedException, ExecutionException {
        super.waitFor();
        metric.stop();
        return this;
    }

    public MeterMetric metric() {
        return metric;
    }
}
