package org.xbib.stream.dsl.unfold;

import org.xbib.stream.Stream;
import org.xbib.stream.delegates.UnfoldedStream;
import org.xbib.stream.dsl.StreamClause;
import org.xbib.stream.dsl.StreamClauseEnv;
import org.xbib.stream.generators.Generator;

/**
 * The clause of {@code unfold} sentences in which a {@link Generator} is configured on the output stream.
 *
 * @param <E> the type of stream elements
 * @author Fabio Simeoni
 */
public class UnfoldThroughClause<E> extends StreamClause<E, StreamClauseEnv<E>> {

    /**
     * Creates an instance from an input {@link Stream}
     *
     * @param stream the stream
     */
    public UnfoldThroughClause(Stream<E> stream) {
        super(new StreamClauseEnv<E>(stream));
    }

    /**
     * Return a {@link Stream} configured with a given {@link Generator}.
     *
     * @param generator the generator
     * @return the stream
     */
    public <E2> UnfoldedStream<E, E2> through(Generator<E, Stream<E2>> generator) {
        return new UnfoldedStream<E, E2>(env.stream(), generator);
    }
}
