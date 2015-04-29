/**
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
package org.xbib.iri.namespace;

import org.xbib.iri.IRI;
import org.xbib.xml.namespace.XmlNamespaceContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class IRINamespaceContext extends XmlNamespaceContext implements CompactingNamespaceContext {

    private static IRINamespaceContext instance;

    private List<String> namespaces;

    private IRINamespaceContext() {
    }

    private IRINamespaceContext(ResourceBundle bundle) {
        Enumeration<String> en = bundle.getKeys();
        while (en.hasMoreElements()) {
            String prefix = en.nextElement();
            String namespace = bundle.getString(prefix);
            addNamespace(prefix, namespace);
        }
    }

    public static IRINamespaceContext getInstance() {
        return getInstance(Locale.getDefault(), Thread.currentThread().getContextClassLoader());
    }

    public static IRINamespaceContext getInstance(Locale locale, ClassLoader classLoader) {
        if (instance == null) {
            try {
                instance = new IRINamespaceContext(ResourceBundle.getBundle("org.xbib.xml.namespaces", locale, classLoader));
            } catch (MissingResourceException e) {
                instance = new IRINamespaceContext();
            }
        }
        return instance;
    }

    public static IRINamespaceContext newInstance() {
        return new IRINamespaceContext();
    }

    public void addNamespace(String prefix, String namespace) {
        super.addNamespace(prefix, namespace);
        namespaces = new ArrayList<String>(getNamespaces().values());
        // sort from longest to shortest for matching
        Collections.sort(namespaces, (s1, s2) -> {
            Integer l1 = s1.length();
            Integer l2 = s2.length();
            return -l1.compareTo(l2);
        });
    }

    public IRINamespaceContext add(Map<String,String> map) {
        for (Map.Entry<String,String> e : map.entrySet()) {
            super.addNamespace(e.getKey(), e.getValue());
        }
        namespaces = new ArrayList<String>(getNamespaces().values());
        // sort from longest to shortest for matching
        Collections.sort(namespaces, (s1, s2) -> {
            Integer l1 = s1.length();
            Integer l2 = s2.length();
            return -l1.compareTo(l2);
        });
        return this;
    }

    /**
     * Abbreviate an URI with a full namespace URI to a short form URI with help of
     * the prefix in this namespace context.
     *
     * @param uri the long URI
     * @return a compact short URI or the original URI if there is no prefix in
     * this context
     */
    @Override
    public String compact(IRI uri) {
        return compact(uri, false);
    }

    public String compact(IRI uri, boolean dropfragment) {
        if (uri == null) {
            return null;
        }
        // drop fragment (useful for resource counters in fragments)
        final String s = dropfragment ? new IRI(uri.getScheme(), uri.getSchemeSpecificPart(), null).toString() : uri.toString();
        // search from longest to shortest namespace prefix
        if (namespaces != null) {
            for (String ns : namespaces) {
                if (s.startsWith(ns)) {
                    return getPrefix(ns) + ':' + s.substring(ns.length());
                }
            }
        }
        return s;
    }

    @Override
    public String expand(IRI curie) {
        String ns = getNamespaceURI(curie.getScheme());
        return ns != null ? ns + curie.getSchemeSpecificPart() : curie.toString();
    }

    @Override
    public IRI expandIRI(IRI curie) {
        String ns = getNamespaceURI(curie.getScheme());
        return ns != null ? IRI.builder().curie(ns + curie.getSchemeSpecificPart()).build() : curie;
    }

    @Override
    public IRI expandIRI(String iri) {
        IRI curie = IRI.builder().curie(iri).build();
        String ns = getNamespaceURI(curie.getScheme());
        return ns != null ? IRI.builder().curie(ns + curie.getSchemeSpecificPart()).build() : curie;
    }

    public String getPrefix(IRI uri) {
        return getNamespaceURI(uri.getScheme()) != null ? uri.getScheme() : getPrefix(uri.toString());
    }
}
