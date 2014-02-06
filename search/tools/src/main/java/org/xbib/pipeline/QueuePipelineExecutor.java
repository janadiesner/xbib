
package org.xbib.pipeline;

import org.xbib.pipeline.element.PipelineElement;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * An Queue Pipeline executor. This executor can execute pipelines in parallel
 * and manage a queue that have to be processed by the pipelines.
 *
 * By doing this, the concurrency works on archive entry level, not URI level.
 *
 * Pipelines are created by a pipeline provider.
 * The maximum number of concurrent pipelines is 256.
 * Each pipeline can receive archive entries, which are put into a blocking queue by
 * this executor.
 *
 * @param <T> the result type
 * @param <R> the pipeline request type
 * @param <P> the pipeline type
 * @param <E> the element type
 */
public class QueuePipelineExecutor<T, R extends PipelineRequest, P extends Pipeline<T,R>, E extends PipelineElement>
     implements PipelineExecutor<P> {

    private ExecutorService executorService;

    private PipelineProvider<P> provider;

    private Set<P> pipelines;

    private Collection<Future<T>> futures;

    private PipelineSink<T> sink;

    private CountDownLatch latch;

    private BlockingQueue<E> queue;

    private int concurrency;

    private volatile boolean closed;

    @Override
    public QueuePipelineExecutor concurrency(int concurrency) {
        this.concurrency = concurrency;
        return this;
    }

    @Override
    public QueuePipelineExecutor provider(PipelineProvider<P> provider) {
        this.provider = provider;
        return this;
    }

    @Override
    public QueuePipelineExecutor sink(PipelineSink sink) {
        this.sink = sink;
        return this;
    }

    @Override
    public QueuePipelineExecutor prepare() throws IOException {
        if (provider == null) {
            throw new IllegalArgumentException("no provider set");
        }
        if (executorService == null) {
            this.executorService = Executors.newFixedThreadPool(concurrency);
        }
        if (pipelines == null) {
            this.pipelines = new HashSet();
        }
        if (concurrency < 1) {
            concurrency = 1;
        }
        this.queue = new SynchronousQueue(true); // fair queue
        this.latch = new CountDownLatch(concurrency);
        for (int i = 0; i < Math.min(concurrency, 256); i++) {
            P pipeline = provider.get();
            pipeline.executor((PipelineExecutor<Pipeline<T,R>>) this);
            pipelines.add(pipeline);
        }
        return this;
    }

    /**
     * Usually, the pipelines receive elements from the executor.
     * Here we ensure that all pipelines that receive elements are invoked.
     * This method returns immediately without waiting for the completion of pipelines.
     *
     * @return this executor
     */
    @Override
    public QueuePipelineExecutor execute() throws IOException {
        futures = new LinkedList();
        for (P pipeline : pipelines) {
            futures.add(executorService.submit(pipeline));
        }
        return this;
    }

    /**
     * Wait for all the pipelines executed.
     * @return this executor
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public QueuePipelineExecutor waitFor() throws InterruptedException, ExecutionException {
        for (Future<T> future : futures) {
            T t = future.get();
            if (sink != null)  {
                sink.out(t);
            }
        }
        return this;
    }

    /**
     * Execute a single pipeline. Can be useful if pipelines must be re-executed.
     * @param pipeline the pipeline
     * @return
     */
    @Override
    public QueuePipelineExecutor execute(P pipeline) throws InterruptedException, ExecutionException {
        latch = null;
        pipeline.executor((PipelineExecutor<Pipeline<T, R>>) this);
        pipelines.add(pipeline);
        Future<T> f = executorService.submit(pipeline);
        T t = f.get();
        if (sink != null)  {
            sink.out(t);
        }
        return this;
    }

    public void shutdown(E poisonElement) throws InterruptedException, ExecutionException, IOException {
        if (closed) {
            return;
        }
        closed = true;
        // how many pipelines are still running?
        int active = 0;
        for (P pipeline : pipelines) {
            if (pipeline.stoppedAt() == null) {
                active++;
            }
        }
        for (int i = 0; i < active; i++) {
            queue.offer(poisonElement, 30, TimeUnit.SECONDS);
        }
        waitFor();
        shutdown();
    }

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

    public BlockingQueue<E> queue() {
        return queue;
    }

    /**
     * Count down the latch. Decreases the number of active pipelines.
     * Called from a pipeline when it terminates.
     * @return this executor
     */
    public QueuePipelineExecutor countDown() {
        latch.countDown();
        return this;
    }

    public long canReceive() {
        return latch.getCount();
    }

    /**
     * Get pipelines
     * @return the pipelines
     */
    public Set<P> getPipelines() {
        return pipelines;
    }

}