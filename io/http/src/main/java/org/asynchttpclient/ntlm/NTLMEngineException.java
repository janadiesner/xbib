package org.asynchttpclient.ntlm;

/**
 * Signals NTLM protocol failure.
 */
public class NTLMEngineException extends Exception {

    private static final long serialVersionUID = 6027981323731768824L;

    public NTLMEngineException() {
        super();
    }

    /**
     * Creates a new NTLMEngineException with the specified message.
     *
     * @param message the exception detail message
     */
    public NTLMEngineException(String message) {
        super(message);
    }

    /**
     * Creates a new NTLMEngineException with the specified detail message and cause.
     *
     * @param message the exception detail message
     * @param cause   the <tt>Throwable</tt> that caused this exception, or <tt>null</tt>
     *                if the cause is unavailable, unknown, or not a <tt>Throwable</tt>
     */
    public NTLMEngineException(String message, Throwable cause) {
        super(message, cause);
    }

}
