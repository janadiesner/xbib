package org.xbib.util.concurrent;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * Advantages

 Uses {@link ConcurrentHashMap} for thread safety and visibility.
 Uses {@link CopyOnWriteArrayList} for list thread safety and visibility.
 No blocking in class itself. Instead the backing jdk classes handle blocking for us.
 Blocking has been reduced to key level granularity instead of being at the cache level.

 Disadvantages

 Prone to interleaving. It is weakly consistent and does not guarantee mutually exclusive and atomic calls.
 The {@link remove(K)} call can interleave through the lines of the put method and potentially
 key value pairs can be added back in if a{@link remove(K)} is called part way through the {@link #put(K,V)} call.
 To be strongly consistent the {@link #remove(K)} and {@link #put(K,V)} need to be mutually exclusive.
 *
 * @param <K>
 * @param <V>
 */
public class CopyOnWriteArrayListMultiMap <K, V> {

    private final ConcurrentMap<K, List<V>> cache = new ConcurrentHashMap<K, List<V>>();

    public List<V> get(K k) {
        return cache.get(k);
    }

    public List<V> remove(K k) {
        return cache.remove(k);
    }

    public void put(K k, V v) {
        List<V> list = cache.get(k);
        if (list == null) {
            list = new CopyOnWriteArrayList<V>();
            List<V> oldList = cache.putIfAbsent(k, list);
            if (oldList != null) {
                list = oldList;
            }
        }
        list.add(v);
    }

    public boolean remove(K k, K v) {
        List<V> list = cache.get(k);
        if (list == null) {
            return false;
        }
        if (list.isEmpty()) {
            cache.remove(k);
            return false;
        }
        boolean removed = list.remove(k);
        if (removed && list.isEmpty()) {
            cache.remove(k);
        }
        return removed;
    }

}