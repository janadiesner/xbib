package org.asynchttpclient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simple {@link AsyncHandler} of type {@link Response}
 */
public class AsyncCompletionHandlerBase extends AsyncCompletionHandler<Response> {

    private final static Logger logger = LogManager.getLogger(AsyncCompletionHandlerBase.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    public Response onCompleted(Response response) throws Exception {
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onThrowable(Throwable t) {
        logger.debug(t.getMessage(), t);
    }
}
