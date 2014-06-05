package org.xbib.util.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PartiallyBlockingCopyOnWriteArrayListMultiMap <K, V> {

    private final ConcurrentMap<K, List<V>> cache = new ConcurrentHashMap<K, List<V>>();

    public List<V> get(K k) {
        return cache.get(k);
    }

    public List<V> remove(K k) {
        synchronized (cache) {
            return cache.remove(k);
        }
    }

    public void put(K k, V v) {
        List<V> list = Collections.singletonList(v);
        List<V> oldList = cache.putIfAbsent(k, list);
        if (oldList != null) {
            synchronized (cache) {
                list = cache.get(k);
                if (list == null || list.isEmpty()) {
                    list = new ArrayList<V>();
                } else {
                    list = new ArrayList<V>(list);
                }
                list.add(v);
                cache.put(k, list);
            }
        }
    }

    public boolean remove(K k, K v) {
        List<V> list = cache.get(k);
        if (list == null) {
            return false;
        }
        synchronized (cache) {
            list = cache.get(k);
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

}