
package org.xbib.pipeline;

public interface PipelineSink<T> {

    void out(T t);
}
