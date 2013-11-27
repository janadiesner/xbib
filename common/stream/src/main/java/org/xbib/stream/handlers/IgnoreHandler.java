package org.xbib.stream.handlers;

/**
 * A {@link org.xbib.stream.handlers.FaultHandler} that silently absorbs all failures.
 */
public class IgnoreHandler implements FaultHandler {

    @Override
    public void handle(RuntimeException failure) {
    }
}
