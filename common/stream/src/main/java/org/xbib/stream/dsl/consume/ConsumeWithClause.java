package org.xbib.stream.dsl.consume;

import org.xbib.stream.Callback;
import org.xbib.stream.Stream;
import org.xbib.stream.StreamConsumer;
import org.xbib.stream.dsl.StreamClause;
import org.xbib.stream.dsl.StreamClauseEnv;

/**
 * The clause of {@code consume} sentences in which a {@link Callback} is configured on the input stream.
 *
 * @param <E> the type of stream elements
 * @author Fabio Simeoni
 */
public class ConsumeWithClause<E> extends StreamClause<E, StreamClauseEnv<E>> {

    /**
     * Creates an instance from an input {@link Stream}
     *
     * @param stream the stream
     */
    public ConsumeWithClause(Stream<E> stream) {
        super(new StreamClauseEnv<E>(stream));
    }

    /**
     * Return a {@link Stream} configured with a given {@link Callback}.
     *
     * @param consumer the consumer
     */
    public <E2> void with(Callback<E> consumer) {
        new StreamConsumer<E>(env.stream(), consumer).start();
    }
}
