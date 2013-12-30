
package org.xbib.metrics;

import io.netty.util.internal.chmv8.LongAdder;

public class CounterMetric implements Metric {

    private final LongAdder counter = new LongAdder();

    public void inc() {
        counter.increment();
    }

    public void inc(long n) {
        counter.add(n);
    }

    public void dec() {
        counter.decrement();
    }

    public void dec(long n) {
        counter.add(-n);
    }

    public long count() {
        return counter.sum();
    }
}