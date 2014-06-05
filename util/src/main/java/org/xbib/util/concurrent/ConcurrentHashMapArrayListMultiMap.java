package org.xbib.util.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Advantages

 Strongly consistent.
 Uses concurrent hash map so we can have non-blocking read.

 Disadvantages

 The synchronisation lock blocks on the entire cache.
 The blocking calls are entirely blocking so all paths through them will block.
 Concurrent hash map is blocking itself although at a fine grained level using stripes.
 * @param <K>
 * @param <V>
 */
public class ConcurrentHashMapArrayListMultiMap<K, V> {

    private final ConcurrentMap<K, List<V>> cache = new ConcurrentHashMap<K, List<V>>();

    public List<V> get(K k) {
        return cache.get(k);
    }

    public synchronized List<V> remove(K k) {
        return cache.remove(k);
    }

    public synchronized void put(K k, V v) {
        List<V> list = cache.get(k);
        if (list == null || list.isEmpty()) {
            list = new ArrayList<V>();
        } else {
            list = new ArrayList<V>(list);
        }
        list.add(v);
        cache.put(k, list);
    }

    public synchronized boolean remove(K k, K v) {
        List<V> list = cache.get(k);
        if (list == null) {
            return false;
        }
        if (list.isEmpty()) {
            cache.remove(k);
            return false;
        }
        boolean removed = list.remove(v);
        if (removed) {
            if (list.isEmpty()) {
                cache.remove(k);
            } else {
                list = new ArrayList<V>(list);
                cache.put(k, list);
            }
        }
        return removed;
    }

}