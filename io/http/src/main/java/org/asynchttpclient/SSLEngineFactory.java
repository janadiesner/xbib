package org.asynchttpclient;

import javax.net.ssl.SSLEngine;
import java.security.GeneralSecurityException;

/**
 * Factory that creates an {@link javax.net.ssl.SSLEngine} to be used for a single SSL connection.
 */
public interface SSLEngineFactory {
    /**
     * Creates new {@link javax.net.ssl.SSLEngine}.
     *
     * @return new engine
     * @throws java.security.GeneralSecurityException if the SSLEngine cannot be created
     */
    SSLEngine newSSLEngine() throws GeneralSecurityException;
}
