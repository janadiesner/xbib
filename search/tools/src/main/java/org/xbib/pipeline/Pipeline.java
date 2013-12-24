package org.xbib.pipeline;

import java.io.Closeable;
import java.util.Iterator;
import java.util.concurrent.Callable;

/**
 * A pipeline.
 *
 * @param <T> the result parameter
 * @param <R> the request parameter
 */
public interface Pipeline<T,R extends PipelineRequest> extends Callable<T>, Closeable, Iterator<R>  {

    /**
     * Add a listener to the pipeline. The listener is called each time
     * this pipeline processes a new request.
     * @param name the listener name
     * @param listener the listener
     * @return this pipeline
     */
    Pipeline<T,R> addLast(String name, PipelineListener<T,R> listener);

    /**
     * Set the executor of this pipeline.
     *
     * @param executor the executor
     * @return this pipeline
     */
    Pipeline<T,R> executor(PipelineExecutor<Pipeline<T,R>> executor);

    Long count();

    Long size();

    Long startedAt();

    Long stoppedAt();

    Long took();

    Throwable lastException();
}
