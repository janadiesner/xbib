
package org.metastatic.rsync;

/**
 * Signals an exception raised by an @{link GeneratorListener}, @{link
 * MatcherListener}, or @{link RebuilderListener}.
 * <p/>
 * <p>Listener exceptions may contain other exceptions (the "cause") and
 * may be chained together if there are multiple failures accross
 * multiple listeners.
 */
public class ListenerException extends Exception {

    protected ListenerException next;

    protected Throwable cause;

    public ListenerException(Throwable cause) {
        super();
        this.cause = cause;
    }

    public ListenerException(Throwable cause, String msg) {
        super(msg);
        this.cause = cause;
    }

    public ListenerException(String msg) {
        super(msg);
    }

    public ListenerException() {
        super();
    }

    /**
     * Returns the next exception in this chain, or <code>null</code> if
     * there are no other exceptions.
     *
     * @return The next exception.
     */
    public ListenerException getNext() {
        return next;
    }

    /**
     * Sets the next exception in this chain.
     *
     * @param next The next exception.
     */
    public void setNext(ListenerException next) {
        this.next = next;
    }

    /**
     * Gets the cause of this exception, or <code>null</code> if the
     * cause is unknown.
     *
     * @return The cause.
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * Sets the cause of this exception.
     *
     * @param cause The cause of this exception.
     */
    public synchronized Throwable initCause(Throwable cause) {
        Throwable old = this.cause;
        this.cause = cause;
        return old;
    }
}
