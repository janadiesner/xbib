package org.xbib.elements.marc.dialects.mab;

import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;

public class Periodic {

    private final Integer base;

    private final Integer size;

    private final Integer count;

    private final Integer delta;

    public Periodic(Integer base, Integer size, Integer count) {
        this(base, size, count, 0);
    }

    public Periodic(Integer base, Integer size, Integer count, Integer delta) {
        this.base = base;
        this.size = size;
        this.count = count;
        this.delta = delta;
    }

    public Integer base() {
        return base;
    }

    public boolean inPeriod(Integer candidate) {
        return candidate + delta >= base && candidate + delta < base + (count * size);
    }

    public Integer getPeriod(Integer candidate) {
        return inPeriod(candidate) ? (candidate - base) / size : -1;
    }

    public List<Integer> spanPeriods(Integer candidate) {
        if (inPeriod(candidate)) {
            List<Integer> list = newLinkedList();
            for (int i = base; i < base + (count * size); i+=size) {
                list.add(i);
            }
            return list;
        } else {
            return null;
        }
    }
}
