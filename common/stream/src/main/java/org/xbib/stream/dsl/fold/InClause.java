package org.xbib.stream.dsl.fold;

import org.xbib.stream.Stream;
import org.xbib.stream.delegates.FoldedStream;
import org.xbib.stream.dsl.StreamClause;
import org.xbib.stream.dsl.StreamClauseEnv;

/**
 * The clause of {@code fold} sentences in which a fold size is configured for
 * the output stream.
 *
 * @param <E> the type of stream elements
 * @author Fabio Simeoni
 */
public class InClause<E> extends StreamClause<E, StreamClauseEnv<E>> {

    /**
     * Creates an instance with an input {@link Stream}.
     *
     * @param stream the stream
     */
    public InClause(Stream<E> stream) {
        super(new StreamClauseEnv<E>(stream));
    }

    /**
     * Returns a {@link Stream} that folds the element of the input {@link Stream} in lists of a given size.
     *
     * @param foldSize the size
     * @return the stream
     */
    public FoldedStream<E> in(int foldSize) {
        return new FoldedStream<E>(env.stream(), foldSize);
    }
}
