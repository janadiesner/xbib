package org.xbib.re;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class Generics {

    public static <T> List<T> newArrayList() {
        return new ArrayList();
    }

    public static <T> List<T> newArrayList(Collection<? extends T> c) {
        return new ArrayList(c);
    }

    public static <T, S> Map<T, S> newHashMap() {
        return new HashMap();
    }

    public static <T, S> Map<S, List<T>> bucket(Collection<? extends T> c, Function<T, S> f) {
        Map<S, List<T>> buckets = Generics.newHashMap();
        for (T value : c) {
            S key = f.eval(value);
            List<T> bucket = buckets.get(key);
            if (bucket == null) {
                buckets.put(key, bucket = Generics.newArrayList());
            }
            bucket.add(value);
        }
        return buckets;
    }

    public static <T> Collection filter(Collection<? extends T> c, Function<T, Boolean> p) {
        Iterator<? extends T> it = c.iterator();
        while (it.hasNext()) {
            if (!p.eval(it.next())) {
                it.remove();
            }
        }
        return c;
    }
}
