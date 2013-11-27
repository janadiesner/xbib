package org.xbib.stream;

import java.net.URI;
import java.util.Iterator;

/**
 * An {@link java.util.Iterator} over the elements of a dataset of arbitrary origin, including memory, secondary storage, and
 * network.
 * <p/>
 * <p/>
 * <h3>Properties</h3><br>
 * <p/>
 * Streams are:
 * <p/>
 * <p/>
 * <ul>
 * <li><em>addressable</em>: clients may invoke {@link #locator()} to obtain a reference to their address. The use and
 * syntax of locators is implementation-dependent.
 * <li><em>closeable</em>: clients may invoke {@link #close()} to allow implementations to release resources. Clients
 * <em>should</em> invoke {@link #close()} if they do not consume streams in their entirety. Implementations
 * <em>must</em> automatically release their resources when they have been consumed in their entirety.
 * <li><em>fallible</em>: invoking {@link #next()} over streams that originate from secondary storage and remote
 * locations may raise a wide range failures. Some failures may be <em>recoverable</em>, in that subsequent invocations
 * of {@link #next()} <em>may</em> still succeed. Other failures may be <em>unrecoverable</em>, in that subsequent
 * invocations of {@link #next()} are guaranteed to fail too.
 * </ul>
 * <p/>
 * <h3>Implementations</h3><br>
 * <p/>
 * There are predefined implementations transform, fold, and unfold the elements of existing streams (cf.
 * {@link org.xbib.stream.delegates.PipedStream}, {@link org.xbib.stream.delegates.FoldedStream}, {@link org.xbib.stream.delegates.UnfoldedStream}).
 * <p/>
 * <p/>
 * Additional implementations allow modular handling of stream faults and notify interested listeners of stream
 * iteration events (cf. {@link org.xbib.stream.delegates.GuardedStream}, {@link org.xbib.stream.delegates.MonitoredStream}).
 * <p/>
 * <p/>
 * Finally, streams may be published outside the current runtime by implementations of the {@link org.xbib.stream.publishers.StreamPublisher}
 * interface.
 * <p/>
 * <p/>
 * <p/>
 * All the available implementations can be fluently instantiated and configured with an embedded DSL (cf.
 * {@link org.xbib.stream.dsl.Streams}).
 * <p/>
 * <h3>Fault Handling</h3><br>
 * <p/>
 * Clients can implement {@link org.xbib.stream.handlers.FaultHandler}s to specify fault handling policies over streams, and then wrap streams in
 * {@link org.xbib.stream.delegates.GuardedStream}s that apply they policies:
 * <p/>
 * <pre>
 * import static ....Streams.*;
 * ...
 * Stream&lt;T&gt; stream = ...
 *
 * FaultHandler handler = new FaultHandler() {
 *   public void handle(RuntimeException fault) {
 *    ...
 *   }
 * };
 *
 * Stream&lt;T&gt; guarded = guard(stream).with(handler);
 * </pre>
 * <p/>
 * <p/>
 * {@link org.xbib.stream.handlers.FaultHandler}s can ignore faults, rethrow them, rethrow different faults, or use the constant
 * {@link org.xbib.stream.handlers.FaultHandler#iteration} to stop the iteration of the underlying stream (cf. {@link Iteration#stop()})
 * <p/>
 * <p/>
 * <p/>
 * Faults are unchecked exceptions thrown by {@link #next()}, often wrappers around an original cause.
 * {@link org.xbib.stream.handlers.FaultHandler}s can use a fluent API to simplify the task of analysing fault causes (cf. {@link org.xbib.stream.dsl.Faults}):
 * <p/>
 * <pre>
 * FaultHandler handler = new FaultHandler() {
 *  	public void handle(RuntimeException fault) {
 *           try {
 *           	throw causeOf(fault).as(SomeException.class,SomeOtherException.class);
 *           }
 *           catch(SomeException e) {...}
 *           catch(SomeOtherException e) {...}
 *        }
 * };
 * </pre>
 * <p/>
 * <h3>Consumption</h3><br>
 * <p/>
 * Clients may consume streams by explicitly iterating over their elements. Since streams are fallible and closeable,
 * the recommended idiom is the following:
 * <p/>
 * <pre>
 * Stream&lt;T&gt; stream = ...
 * try {
 *   while (stream.hasNext())
 *     ....stream.next()...
 * }
 * finally {
 *  stream.close();
 * }
 * </pre>
 * <p/>
 * Alternatively, clients may provide {@link Callback}s to generic {@link StreamConsumer}s that iterate on
 * behalf of clients. Using the simplifications of the DSL:
 * <p/>
 * <pre>
 * Stream&lt;T&gt; stream = ...
 *
 * Callback&lt;T&gt; callback = new Callback&lt;T&gt;() {
 *  	public void consume(T element) {
 *           ...element...
 *        }
 * };
 *
 * consume(stream).with(callback);
 * </pre>
 * <p/>
 * {@link Callback}s can control iteration through the {@link Iteration} constant (cf. {@link Callback#iteration}):
 * <p/>
 * <pre>
 * Callback&lt;T&gt; callback = new Callback&lt;T&gt;() {
 *  	public void consume(T element) {
 *  	    ...iteration.stop()...
 *  		...
 *        }
 * };
 * </pre>
 *
 * @param <E> the type of elements iterated over
 */
public interface Stream<E> extends Iterator<E> {

    boolean hasNext();

    /**
     * @throws java.util.NoSuchElementException
     *                          if the stream has no more elements or it has been closed
     * @throws org.xbib.stream.exceptions.StreamOpenException
     *                          if the stream cannot be opened
     * @throws RuntimeException if the element cannot be returned
     */
    E next();

    /**
     * Returns the stream locator.
     *
     * @return the locator
     * @throws IllegalStateException if the stream is no longer addressable at the time of invocation.
     */
    URI locator();

    /**
     * Closes the stream unconditionally, releasing any resources that it may be using.
     * <p/>
     * Subsequent invocations of this method have no effect.<br>
     * Subsequents invocations of {@link #hasNext()} return {@code false}.<br>
     * Subsequent invocations of {@link #next()} throw {@link java.util.NoSuchElementException}s.
     * <p/>
     * Failures are logged by implementations and suppressed otherwise.
     */
    void close();

    /**
     * Returns <code>true</code> if the stream has been closed.
     *
     * @return <code>true</code> if the stream has been closed
     */
    boolean isClosed();

}