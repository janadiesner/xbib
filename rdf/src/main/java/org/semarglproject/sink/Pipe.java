package org.semarglproject.sink;

/**
 * Interface for pipeline procecessing units with one sink.
 *
 * @param <S> class of output sink
 */
public interface Pipe<S extends Sink> extends Sink {

}
