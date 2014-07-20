package org.asynchttpclient;

import java.net.URI;
import java.util.List;

/**
 * A class that represent the HTTP response' status line (code + text)
 */
public abstract class HttpResponseStatus {

    private final URI uri;
    protected final AsyncHttpClientConfig config;

    public HttpResponseStatus(URI uri, AsyncHttpClientConfig config) {
        this.uri = uri;
        this.config = config;
    }

    /**
     * Return the request {@link java.net.URI}
     * 
     * @return the request {@link java.net.URI}
     */
    public final URI getUri() {
        return uri;
    }
    
    /**
     * Prepare a {@link Response}
     *
     * @param headers   {@link org.asynchttpclient.HttpResponseHeaders}
     * @param bodyParts list of {@link org.asynchttpclient.HttpResponseBodyPart}
     * @return a {@link Response}
     */
    public abstract Response prepareResponse(HttpResponseHeaders headers, List<HttpResponseBodyPart> bodyParts);

    /**
     * Return the response status code
     * 
     * @return the response status code
     */
    public abstract int getStatusCode();

    /**
     * Return the response status text
     * 
     * @return the response status text
     */
    public abstract String getStatusText();

    /**
     * Protocol name from status line.
     * 
     * @return Protocol name.
     */
    public abstract String getProtocolName();

    /**
     * Protocol major version.
     * 
     * @return Major version.
     */
    public abstract int getProtocolMajorVersion();

    /**
     * Protocol minor version.
     * 
     * @return Minor version.
     */
    public abstract int getProtocolMinorVersion();

    /**
     * Full protocol name + version
     * 
     * @return protocol name + version
     */
    public abstract String getProtocolText();
}
