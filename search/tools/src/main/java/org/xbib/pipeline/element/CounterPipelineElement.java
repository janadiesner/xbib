package org.xbib.pipeline.element;

import org.xbib.pipeline.PipelineRequest;

import java.util.concurrent.atomic.AtomicLong;

public class CounterPipelineElement implements PipelineElement<AtomicLong>, PipelineRequest {

    private AtomicLong l;

    @Override
    public AtomicLong get() {
        return l;
    }

    @Override
    public CounterPipelineElement set(AtomicLong atomicLong) {
        this.l = atomicLong;
        return this;
    }

    @Override
    public Long size() {
        return l.longValue();
    }

    @Override
    public String toString() {
        return l.toString();
    }
}
