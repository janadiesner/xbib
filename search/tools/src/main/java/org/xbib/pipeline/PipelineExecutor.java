package org.xbib.pipeline;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

/**
 * The Pipeline Executor can execute provided pipelines in parallel.
 *
 * @param <P> the pipeline type
 */
public interface PipelineExecutor<P extends Pipeline> {

    /**
     * Set the concurrency of this pipeline executor
     * @param concurrency the concurrency, must be a positive integer
     * @return this executor
     */
    PipelineExecutor concurrency(int concurrency);

    /**
     * Set the provider of this pipeline executor
     * @param provider the pipeline provider
     * @return this executor
     */
    PipelineExecutor provider(PipelineProvider<P> provider);

    /**
     * Return pipelines
     * @return the pipelines
     */
    Collection<P> getPipelines();

    /**
     * Optional pipeline sink
     * @param sink the pipeline sink
     * @return this executor
     */
    PipelineExecutor sink(PipelineSink sink);

    /**
     * Prepare the pipeline execution.
     * @return this executor
     */
    PipelineExecutor prepare();

    /**
     * Execute the pipelines.
     * @return this executor
     */
    PipelineExecutor execute();

    /**
     * Execute the pipelines.
     * @return this executor
     * @throws InterruptedException
     * @throws ExecutionException
     */
    PipelineExecutor waitFor() throws InterruptedException, ExecutionException;

    /**
     * Execute a single pipeline.
     * @param pipeline the piepline
     * @return this executor
     * @throws InterruptedException
     * @throws ExecutionException
     */
    PipelineExecutor execute(P pipeline) throws InterruptedException, ExecutionException;

    /**
     * Shut down this pipeline executor
     * @throws InterruptedException
     */
    void shutdown() throws InterruptedException, ExecutionException, IOException;
}
