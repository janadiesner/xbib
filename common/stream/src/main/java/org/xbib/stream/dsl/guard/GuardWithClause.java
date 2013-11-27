package org.xbib.stream.dsl.guard;

import org.xbib.stream.Stream;
import org.xbib.stream.delegates.GuardedStream;
import org.xbib.stream.dsl.StreamClause;
import org.xbib.stream.dsl.StreamClauseEnv;
import org.xbib.stream.handlers.FaultHandler;

/**
 * The clause of {@code fold} sentences of the {@link Stream} DSL in which a {@link FaultHandler} is configured on
 * the output stream.
 *
 * @param <E> the type of stream elements
 */
public class GuardWithClause<E> extends StreamClause<E, StreamClauseEnv<E>> {

    /**
     * Creates an instance with an input {@link Stream}.
     *
     * @param stream the stream
     */
    public GuardWithClause(Stream<E> stream) {
        super(new StreamClauseEnv<E>(stream));
    }

    /**
     * Returns a {@link Stream} with configured with a given {@link FaultHandler}.
     *
     * @param handler the handler
     * @return the stream
     */
    public GuardedStream<E> with(FaultHandler handler) {
        return new GuardedStream<E>(env.stream(), handler);
    }
}
