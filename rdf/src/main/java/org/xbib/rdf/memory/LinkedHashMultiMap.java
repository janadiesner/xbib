/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.rdf.memory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class LinkedHashMultiMap<K, V> implements MultiMap<K, V> {

    private final Map<K, Set<V>> map = new LinkedHashMap<K, Set<V>>();

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> put(K key, V value) {
        Set<V> set = map.get(key);
        if (set == null) {
            set = new LinkedHashSet<V>();
        }
        set.add(value);
        return map.put(key, set);
    }

    @Override
    public void putAll(K key, Collection<V> values) {
        Set<V> set = map.get(key);
        if (set == null) {
            set = new LinkedHashSet<V>();
            map.put(key, set);
        }
        set.addAll(values);
    }

    @Override
    public Collection<V> get(K key) {
        return map.get(key);
    }

    @Override
    public Set<V> remove(K key) {
        return map.remove(key);
    }

    @Override
    public Set<V> remove(K key, V value) {
        Set<V> set = map.get(key);
        if (set != null) {
            set.remove(value);
        }
        return set;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof LinkedHashMultiMap && map.equals(((LinkedHashMultiMap) obj).map);
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


   
    
    
    