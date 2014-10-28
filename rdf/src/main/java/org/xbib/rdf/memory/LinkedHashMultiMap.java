package org.xbib.rdf.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class LinkedHashMultiMap<K, V> implements MultiMap<K, V> {
    private final Map<K, Set<V>> map = new LinkedHashMap<K, Set<V>>();

    public Collection<V> put(K key, V value) {
        Set<V> set = map.get(key);
        if (set == null) {
            set = new LinkedHashSet<V>();
        }
        set.add(value);
        return map.put(key, set);
    }

    public Collection<V> get(K key) {
        return map.get(key);
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public boolean containsValue(V value) {
        for (K key : map.keySet()) {
            if (map.get(key).contains(value)) {
                return true;
            }
        }
        return false;
    }

    public Set<V> remove(K key) {
        return map.remove(key);
    }

    public void clear() {
        map.clear();
    }

    public Collection<V> values() {
        Collection<V> l = new ArrayList<V>();
        map.values().stream().forEach(l::addAll);
        return l;
    }

    public void removeAll(K key) {
        if (map.containsKey(key)) {
            map.get(key).clear();
        }
    }

    public void putAll(K key, Collection<V> values) {
        Set<V> set = map.get(key);
        if (set == null) {
            set = new LinkedHashSet<V>();
            map.put(key, set);
        }
        set.addAll(values);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof MultiMap) {
            final MultiMap<K, V> other = (MultiMap<K, V>) obj;
            return !(keySet() != other.keySet() && (keySet() == null || !keySet().equals(other.keySet()))) && !(values() != other.values() && (values() == null || !values().equals(other.values())));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public String toString() {
        return map.toString();
    }
}


   
    
    
    