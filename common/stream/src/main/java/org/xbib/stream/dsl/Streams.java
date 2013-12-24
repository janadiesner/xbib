package org.xbib.stream.dsl;

import org.xbib.stream.Stream;
import org.xbib.stream.adapters.IteratorAdapter;
import org.xbib.stream.adapters.IteratorStream;
import org.xbib.stream.dsl.consume.ConsumeWithClause;
import org.xbib.stream.dsl.fold.InClause;
import org.xbib.stream.dsl.guard.GuardWithClause;
import org.xbib.stream.dsl.listen.MonitorWithClause;
import org.xbib.stream.dsl.pipe.PipeThroughClause;
import org.xbib.stream.dsl.unfold.UnfoldThroughClause;
import org.xbib.stream.generators.NoOpGenerator;
import org.xbib.stream.handlers.FaultHandler;
import org.xbib.stream.handlers.IgnoreHandler;
import org.xbib.stream.handlers.RethrowHandler;
import org.xbib.stream.handlers.RethrowUnrecoverableHandler;
import org.xbib.stream.handlers.StopFastHandler;
import org.xbib.stream.test.FallibleIterator;

import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * The definitions of an eDSL of stream and stream-related expressions.
 */
public class Streams {

    /**
     * Starts a sentence to consume a {@link Stream}
     *
     * @param stream the stream
     * @return the next clause of the sentence
     */
    public static <E> ConsumeWithClause<E> consume(Stream<E> stream) {
        return new ConsumeWithClause<E>(stream);
    }

    /**
     * Converts an {@link java.util.Iterator} to a {@link Stream}.
     *
     * @param itarator the iterator
     * @return the stream
     */
    public static <E> Stream<E> convert(Iterator<E> itarator) {
        return new IteratorStream<E>(itarator);
    }

    /**
     * Converts a custom {@link IteratorAdapter} to a {@link Stream}.
     *
     * @param adapter the adapter
     * @return the stream
     */
    public static <E> IteratorStream<E> convert(IteratorAdapter<E> adapter) {
        return new IteratorStream<E>(adapter);
    }

    /**
     * Converts an {@link Iterable} to a {@link Stream}.
     *
     * @param iterable the iterable
     * @return the stream
     */
    public static <E> Stream<E> convert(Iterable<E> iterable) {
        return convert(iterable.iterator());
    }

    /**
     * Converts one or more elements into a {@link Stream}.
     *
     * @param elements the elements
     * @return the stream
     */
    public static <E> Stream<E> convert(E... elements) {
        return convert(asList(elements));
    }

    /**
     * Converts a mixture of exceptions and elements of a given type to a {@link Stream} of a that type.
     * It's the client's responsibility to ensure that the elements that are not exceptions are homogeneously typed as the type indicated in input.
     *
     * @param clazz    the stream type
     * @param elements the elements
     * @return the stream
     */
    public static <E> Stream<E> convertWithFaults(Class<E> clazz, Object... elements) {
        return convertWithFaults(clazz, asList(elements));
    }

    /**
     * Converts a mixture of exceptions and elements of a given type to a {@link Stream} of a that type.
     * It's the client's responsibility to ensure that the elements that are not exceptions are homogeneously typed as the type indicated in input.
     *
     * @param clazz    the stream type
     * @param elements the elements
     * @return the stream
     */
    public static <E> Stream<E> convertWithFaults(Class<E> clazz, List<? extends Object> elements) {

        return convert(new FallibleIterator<E>(clazz, elements));
    }

    /**
     * Starts a sentence to produce a {@link Stream} generated from another {@link Stream}.
     *
     * @param stream the input stream.
     * @return the next clause of the sentence
     */
    public static <E> PipeThroughClause<E> pipe(Stream<E> stream) {
        return new PipeThroughClause<E>(stream);
    }

    /**
     * Starts a sentence to produce a {@link Stream} that groups of elements of another {@link Stream}.
     *
     * @param stream the input stream.
     * @return the next clause of the sentence
     */
    public static <E> InClause<E> fold(Stream<E> stream) {
        return new InClause<E>(stream);
    }

    /**
     * Starts a sentence to produce a {@link Stream} that unfolds the elements of another {@link Stream}.
     *
     * @param stream the input stream.
     * @return the next clause of the sentence
     */
    public static <E> UnfoldThroughClause<E> unfold(Stream<E> stream) {
        return new UnfoldThroughClause<E>(stream);
    }

    /**
     * Starts a sentence to produce a {@link Stream} that controls the error raised by another {@link Stream}.
     *
     * @param stream the input stream.
     * @return the next clause of the sentence
     */
    public static <E> GuardWithClause<E> guard(Stream<E> stream) {
        return new GuardWithClause<E>(stream);
    }

    /**
     * Starts a sentence to produce a {@link Stream} that notifies key events in the iteration of another {@link Stream}.
     *
     * @param stream the input stream.
     * @return the next clause of the sentence
     */
    public static <E> MonitorWithClause<E> monitor(Stream<E> stream) {
        return new MonitorWithClause<E>(stream);
    }

    /**
     * A {@link NoOpGenerator}.
     */
    public static NoOpGenerator<String> noserialiser = new NoOpGenerator<String>();

    /**
     * Returns a {@link NoOpGenerator}.
     *
     * @return the generator
     */
    public static <E> NoOpGenerator<E> noOp(Stream<E> stream) {
        return new NoOpGenerator<E>();
    }

    /**
     * A {@link RethrowHandler} for failure handling.
     */
    public static FaultHandler RETHROW_POLICY = new RethrowHandler();

    /**
     * A {@link RethrowUnrecoverableHandler} for failure handling.
     */
    public static FaultHandler RETHROW_UNRECOVERABLE_POLICY = new RethrowUnrecoverableHandler();

    /**
     * A {@link StopFastHandler} for failure handling.
     */
    public static FaultHandler STOPFAST_POLICY = new StopFastHandler();

    /**
     * A {@link IgnoreHandler} for failure handling.
     */
    public static FaultHandler IGNORE_POLICY = new IgnoreHandler();
}
