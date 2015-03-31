package org.snmp4j;

/**
 * The <code>DefaultTimeoutModel</code> implements a timeout model that uses
 * constant timeouts between retries.
 * <p>
 * The total time waited before a request is timed out is therefore:
 * <code>(totalNumberOfRetries + 1) * targetTimeout</code> where each (re)try
 * is timed out after <code>targetTimeout</code> milliseconds.
 */
public class DefaultTimeoutModel implements TimeoutModel {

    public DefaultTimeoutModel() {
    }

    public long getRetryTimeout(int retryCount,
                                int totalNumberOfRetries, long targetTimeout) {
        return targetTimeout;
    }

    public long getRequestTimeout(int totalNumberOfRetries, long targetTimeout) {
        return (totalNumberOfRetries + 1) * targetTimeout;
    }
}
