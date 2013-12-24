package org.xbib.pipeline;

public class EndOfPipeline extends RuntimeException {

    public EndOfPipeline() {
        super();
    }

    public EndOfPipeline(String msg) {
        super(msg);
    }

    public EndOfPipeline(String msg, Throwable t) {
        super(msg, t);
    }
}
