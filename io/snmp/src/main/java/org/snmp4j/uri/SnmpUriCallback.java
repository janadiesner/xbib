package org.snmp4j.uri;

import java.net.URI;

/**
 * The <code>SnmpUriCallback</code> interface is used by asynchronous
 * methods of the {@link org.snmp4j.uri.SnmpURI} class to provide instances of
 * {@link SnmpUriResponse} to the caller.
 */
public interface SnmpUriCallback {

    /**
     * Process a response on the request
     *
     * @param response   a {@link SnmpUriResponse} instance with some or all
     *                   of the requested data or an error status.
     *                   If the {@link org.snmp4j.uri.SnmpUriResponse#getResponseType()}
     *                   is {@link org.snmp4j.uri.SnmpUriResponse.Type#NEXT} then
     *                   additional calls for this request will follow, otherwise not.
     * @param url        the URI that was used as request for this response.
     * @param userObject an arbitrary object provided on the asynchronous call
     *                   on the request processor.
     * @return <code>true</code> if the request should be cancelled,
     * <code>false</code> otherwise.
     */
    boolean onResponse(SnmpUriResponse response, URI url, Object userObject);

}

