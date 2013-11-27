package org.xbib.stream.handlers;

/**
 * A {@link org.xbib.stream.handlers.FaultHandler} that rethrows all failures (i.e. does not handle any).
 */
public class RethrowHandler implements FaultHandler {

    @Override
    public void handle(RuntimeException failure) {

        throw failure;
    }
}
