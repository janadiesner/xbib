
package org.xbib.pipeline;

public interface PipelineListener<T,R extends PipelineRequest> {

    /**
     * Receive a pipeline event.
     *
     * @param executor
     * @param pipeline
     * @param request
     */
    void listen(PipelineExecutor<Pipeline<T,R>> executor, Pipeline<T,R> pipeline, R request);
}
