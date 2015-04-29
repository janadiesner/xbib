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
package org.xbib.xml.namespace;

import javax.xml.namespace.NamespaceContext;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Contains a simple context for XML namespaces
 */
public class XmlNamespaceContext implements NamespaceContext {

    private static final String DEFAULT_RESOURCE = "org.xbib.xml.namespaces";

    // sort namespace by length in descending order, useful for compacting prefix
    private final SortedMap<String, String> namespaces = new TreeMap<String, String>();

    private final SortedMap<String, Set<String>> prefixes = new TreeMap<String, Set<String>>();

    protected XmlNamespaceContext() {
    }

    protected XmlNamespaceContext(ResourceBundle bundle) {
        Enumeration<String> en = bundle.getKeys();
        while (en.hasMoreElements()) {
            String prefix = en.nextElement();
            String namespace = bundle.getString(prefix);
            addNamespace(prefix, namespace);
        }
    }

    private static String bundleName() {
        return DEFAULT_RESOURCE;
    }

    /**
     * Empty namespace context.
     *
     * @return an XML namespace context
     */
    public static XmlNamespaceContext newInstance() {
        return new XmlNamespaceContext();
    }

    public static XmlNamespaceContext getDefaultInstance() {
        return newInstance(bundleName());
    }

    public static XmlNamespaceContext newInstance(String bundleName) {
        return newInstance(bundleName, Locale.getDefault(), Thread.currentThread().getContextClassLoader());
    }

    public static XmlNamespaceContext newInstance(String bundleName, Locale locale, ClassLoader classLoader) {
        try {
            return new XmlNamespaceContext(ResourceBundle.getBundle(bundleName, locale, classLoader));
        } catch (MissingResourceException e) {
            return new XmlNamespaceContext();
        }
    }

    public void addNamespace(String prefix, String namespace) {
        namespaces.put(prefix, namespace);
        if (prefixes.containsKey(namespace)) {
            prefixes.get(namespace).add(prefix);
        } else {
            Set<String> set = new HashSet<String>();
            set.add(prefix);
            prefixes.put(namespace, set);
        }
    }

    public void removeNamespace(String prefix) {
        prefixes.remove(namespaces.remove(prefix));
    }

    public SortedMap<String, String> getNamespaces() {
        return namespaces;
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            return null;
        }
        return namespaces.containsKey(prefix) ? namespaces.get(prefix) : null;
    }

    @Override
    public String getPrefix(String namespaceURI) {
        Iterator<String> it = getPrefixes(namespaceURI);
        return it != null && it.hasNext() ? it.next() : null;
    }

    @Override
    public Iterator<String> getPrefixes(String namespace) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace URI cannot be null");
        }
        return prefixes.containsKey(namespace) ?
                prefixes.get(namespace).iterator() : null;
    }

    public String toString() {
        return namespaces.toString();
    }

}
