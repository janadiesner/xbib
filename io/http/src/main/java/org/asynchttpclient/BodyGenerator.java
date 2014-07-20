package org.asynchttpclient;

import java.io.IOException;

/**
 * Creates a request body.
 */
public interface BodyGenerator {

    /**
     * Creates a new instance of the request body to be read. While each invocation of this method is supposed to create
     * a fresh instance of the body, the actual contents of all these body instances is the same. For example, the body
     * needs to be resend after an authentication challenge of a redirect.
     *
     * @return The request body, never {@code null}.
     * @throws java.io.IOException If the body could not be created.
     */
    Body createBody()
            throws IOException;

}
