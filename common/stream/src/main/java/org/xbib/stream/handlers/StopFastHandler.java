package org.xbib.stream.handlers;


/**
 * A {@link org.xbib.stream.handlers.FaultHandler} that silently stops iteration at the first occurrence of any failure.
 */
public class StopFastHandler implements FaultHandler {

    @Override
    public void handle(RuntimeException failure) {

        iteration.stop();
    }
}
