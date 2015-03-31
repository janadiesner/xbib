package org.semarglproject.sink;

import java.io.IOException;

/**
 * Base class for pipeline procecessing blocks with one sink.
 *
 * @param <S> class of output sink
 */
public abstract class AbstractPipe<S extends Sink> implements Pipe {

    protected final S sink;

    protected AbstractPipe(S sink) {
        this.sink = sink;
    }

    @Override
    public void startStream() throws IOException {
        sink.startStream();
    }

    @Override
    public void endStream() throws IOException {
        sink.endStream();
    }

}
