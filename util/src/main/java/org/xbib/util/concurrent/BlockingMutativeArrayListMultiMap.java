package org.xbib.util.concurrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Advantages

 Strongly consistent.
 Doesn’t allocate any more than it needs to (unlike the copy on write pattern).

 Disadvantages

 Very poor performance.
 Uses a hashmap which isn’t thread safe so offers no visibility guarantees.
 All calls – reads/writes are blocking.
 All paths through the blocking calls are blocking.
 *
 * @param <K>
 * @param <V>
 */
public class BlockingMutativeArrayListMultiMap <K, V> {

    private final Map<K, List<V>> cache = new HashMap<K, List<V>>();

    public synchronized List<V> get(K k) {
        return cache.get(k);
    }

    public synchronized List<V> remove(K k) {
        return cache.remove(k);
    }

    public synchronized void put(K k, V v) {
        List<V> list = cache.get(k);
        if (list == null) {
            list = new ArrayList<V>();
            cache.put(k, list);
        }
        list.add(v);
    }

    public synchronized boolean remove(K k, V v) {
        List<V> list = cache.get(k);
        if (list == null) {
            return false;
        }
        if (list.isEmpty()) {
            cache.remove(k);
            return false;
        }
        boolean removed = list.remove(v);
        if (removed && list.isEmpty()) {
            cache.remove(k);
        }
        return removed;
    }

}