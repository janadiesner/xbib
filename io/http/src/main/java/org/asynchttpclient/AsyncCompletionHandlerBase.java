package org.asynchttpclient;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

/**
 * Simple {@link AsyncHandler} of type {@link Response}
 */
public class AsyncCompletionHandlerBase extends AsyncCompletionHandler<Response> {

    private final static Logger logger = LoggerFactory.getLogger(AsyncCompletionHandlerBase.class.getName());

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
