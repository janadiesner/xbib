
package org.xbib.pipeline;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SimplePipelineExecutor<T, R extends PipelineRequest, P extends Pipeline<T,R>>
    implements PipelineExecutor<P> {

    private final static Logger logger = LoggerFactory.getLogger(SimplePipelineExecutor.class.getSimpleName());

    private ExecutorService executorService;

    private Collection<P> pipelines;

    private Collection<Future<T>> futures;

    private PipelineProvider<P> provider;

    private PipelineSink<T> sink;

    private int concurrency;

    public SimplePipelineExecutor() {
    }

    @Override
    public SimplePipelineExecutor concurrency(int concurrency) {
        this.concurrency = concurrency;
        return this;
    }

    @Override
    public SimplePipelineExecutor provider(PipelineProvider<P> provider) {
        this.provider = provider;
        return this;
    }

    @Override
    public SimplePipelineExecutor sink(PipelineSink sink) {
        this.sink = sink;
        return this;
    }

    @Override
    public SimplePipelineExecutor prepare() {
        if (provider == null) {
            throw new IllegalStateException("no provider set");
        }
        if (executorService == null) {
            this.executorService = Executors.newFixedThreadPool(concurrency);
        }
        this.pipelines = new LinkedList();
        if (concurrency < 1) {
            concurrency = 1;
        }
        for (int i = 0; i < Math.min(concurrency, 256); i++) {
            P pipeline = provider.get();
            pipeline.executor((PipelineExecutor<Pipeline<T,R>>) this);
            pipelines.add(pipeline);
        }
        return this;
    }

    @Override
    public SimplePipelineExecutor execute() {
        if (pipelines == null) {
            prepare();
        }
        if (pipelines.isEmpty()) {
            throw new IllegalStateException("pipelines empty");
        }
        if (executorService == null) {
            this.executorService = Executors.newFixedThreadPool(concurrency);
        }
        futures = new LinkedList();
        for (Callable<T> pipeline : pipelines) {
            futures.add(executorService.submit(pipeline));
        }
        return this;
    }

    @Override
    public SimplePipelineExecutor execute(P pipeline) throws InterruptedException, ExecutionException {
        if (executorService == null) {
            this.executorService = Executors.newFixedThreadPool(concurrency);
        }
        pipeline.executor((PipelineExecutor<Pipeline<T,R>>) this);
        futures = new LinkedList();
        futures.add(executorService.submit(pipeline));
        waitFor();
        return this;
    }

    /**
     * Get pipelines
     * @return the pipelines
     */
    @Override
    public Collection<P> getPipelines() {
        return pipelines;
    }

    /**
     * Wait for all results.
     *
     * @return this pipeline service
     * @throws InterruptedException
     * @throws ExecutionException 
     */
    @Override
    public SimplePipelineExecutor waitFor() throws InterruptedException, ExecutionException {
        if (executorService == null) {
            return this;
        }
        if (pipelines == null) {
            return this;
        }
        if (futures == null) {
            return this;
        }
        if (futures.isEmpty()) {
            return this;
        }
        for (Future<T> future : futures) {
            T t = future.get();
            if (sink != null && !future.isCancelled()) {
                try {
                    sink.out(t);
                } catch (Exception e) {
                    logger.error("sink failed for {}", t);
                }
            } else {
                logger.info("no sink for {}", t);
            }
        }
        return this;
    }

    public Long getTotalCount() {
        long totalCount = 0L;
        for (P pipeline : pipelines) {
            totalCount += pipeline.count();
        }
        return totalCount;
    }

    public Long getTotalSize() {
        long totalSize = 0L;
        for (P pipeline : pipelines) {
            totalSize += pipeline.size();
        }
        return totalSize;
    }

    @Override
    public void shutdown() throws InterruptedException, IOException {
        if (executorService == null) {
            return;
        }
        executorService.shutdown();
        if (!executorService.awaitTermination(15, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
            if (!executorService.awaitTermination(15, TimeUnit.SECONDS)) {
                throw new IOException("pool did not terminate");
            }
        }
    }
}
