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
package org.xbib.elements;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.xbib.elements.scripting.ScriptElement;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newTreeMap;

/**
 * Abstract specification
 */
public abstract class AbstractSpecification implements Specification {

    private final static Logger logger = LoggerFactory.getLogger(AbstractSpecification.class.getName());

    private final static Map<String, Map> maps = newTreeMap();

    private final static int DEFAULT_BUFFER_SIZE = 8192;

    public AbstractSpecification() {
    }

    public Map<String,Map> map() {
        return maps;
    }

    public synchronized Map getElementMap(ClassLoader cl, String path, String format)
            throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException,
            NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        if (!maps.containsKey(format)) {
            init(cl, path, format);
        }
        return maps.get(format);
    }

    private void init(ClassLoader cl, String path, String format)
            throws IOException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, NoSuchMethodException, IllegalArgumentException,
            InvocationTargetException {
        logger.info("initializing spec from {}", path + format + ".json");
        InputStream resource = loadResource(cl, path + format + ".json");
        if (resource == null) {
            String msg = "format " + format + " not found: " + path + format + ".json";
            throw new IOException(msg);
        }
        final Map elementMap = newTreeMap();
        Map<String, Map<String, Object>> defs =
                new ObjectMapper().configure(Feature.ALLOW_COMMENTS, true).readValue(resource, Map.class);
        init(cl, path, format, elementMap, defs);
    }

    private void init(ClassLoader cl, String path, String format, Map elementMap, Map<String, Map<String, Object>> defs)
                throws IOException, ClassNotFoundException, InstantiationException,
                IllegalAccessException, NoSuchMethodException, IllegalArgumentException,
                InvocationTargetException {
        for (String key : defs.keySet()) {
            Map<String, Object> struct = defs.get(key);
            Element element = null;
            Collection<String> values = (Collection<String>) struct.get("values");
            String type = (String) struct.get("_type");
            if (type != null && type.startsWith("application/x-")) {
                String language = type.substring("application/x-".length());
                String script = (String) struct.get("script");
                String invocable = (String) struct.get("class");
                String source = (String) struct.get("source");
                if (script != null) {
                    InputStream input = loadResource(cl, path + script);
                    if (input == null) {
                        throw new IOException(path + script + " not found: " + path + script);
                    }
                    try (InputStreamReader reader = new InputStreamReader(input, "UTF-8")) {
                        ScriptElement scriptElement = new ScriptElement(language, getString(reader), invocable);
                        scriptElement.setSettings(struct);
                        element = scriptElement.getElement();
                    }
                } else if (source != null) {
                    ScriptElement scriptElement = new ScriptElement(language, source, invocable);
                    scriptElement.setSettings(struct);
                    element = scriptElement.getElement();
                }
            } else {
                // sub resource in classpath?
                InputStream in = loadResource(cl, path + key);
                if (in != null) {
                    Map<String, Map<String, Object>> children =
                            new ObjectMapper().configure(Feature.ALLOW_COMMENTS, true).readValue(in, Map.class);
                    // recursive
                    init(cl, path + key, format, elementMap, children);
                } else {
                    // load class in this xbib element package
                    String clazzName = getPackageFromPath(path + format) + key;
                    Class clazz = loadClass(cl, clazzName);
                    if (clazz == null) {
                        // custom class name, try without package
                        clazz = loadClass(cl, key);
                    }
                    if (clazz != null) {
                        Method factoryMethod = clazz.getDeclaredMethod("getInstance");
                        if (factoryMethod == null) {
                            logger.error("no 'getInstance' method declared in {}" + clazz.getName());
                        } else {
                            try {
                                element = (Element) factoryMethod.invoke(null);
                            } catch (NullPointerException e) {
                                logger.error("'getInstance' method declared not static in {}" + clazz.getName());
                            } catch (ClassCastException e) {
                                logger.error("not an org.xbib.elements.Element class: " + clazz.getName());
                            }
                        }
                        if (element != null) {
                            // set settings
                            element.setSettings(struct);
                        }
                    }
                }
            }
            // connect each value to an element class
            if (values != null) {
                for (String value : values) {
                    addSpec(value, element, elementMap);
                }
            }
        }
        maps.put(format, elementMap);
    }

    public Element getElement(String spec, Map map) {
        int pos = spec != null ? spec.indexOf('$') : 0;
        String h = pos > 0 ? spec.substring(0, pos) : null;
        String t = pos > 0 ? spec.substring(pos+1) : spec;
        return getElement(h, t, map);
    }

    public Element getElementBySpec(String spec, Map map) {
        return getElement(null, spec, map);
    }

    private Element getElement(String head, String tail, Map map) {
        if (head == null) {
            return (Element)map.get(tail);
        }
        int pos = tail != null ? tail.indexOf('$') : 0;
        String h = pos > 0 ? tail.substring(0, pos) : null;
        String t = pos > 0 ? tail.substring(pos+1) : tail;
        Object o = map.get(head);
        if (o != null) {
            return o instanceof Map ? getElement(h, t, (Map)o) :
                   o instanceof Element ? (Element)o : null;
        } else {
            return null;
        }
    }

    private String getPackageFromPath(String path) {
        // some hackery ahead
        String packageName = path.replace('/', '.');
        if (packageName.charAt(0) == '.') {
            packageName = packageName.substring(1);
        }
        if (packageName.charAt(packageName.length() - 1) != '.') {
            packageName = packageName + '.';
        }
        return packageName;
    }

    /*public String getString(InputStream input, String encoding) throws IOException {
        return getString(new InputStreamReader(input, encoding));
    }*/

    private String getString(Reader input) throws IOException {
        StringWriter sw = new StringWriter();
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        int n;
        while ((n = input.read(buffer)) != -1) {
            sw.write(buffer, 0, n);
        }
        return sw.toString();
    }

    private InputStream loadResource(ClassLoader cl, String resourcePath) {
        // load from root of jar
        InputStream in = cl.getResourceAsStream(resourcePath);
        if (in == null) {
            // load from same path as class ElementMap
            in = AbstractSpecification.class.getResourceAsStream(resourcePath);
            if (in == null) {
                // last resort: load from system class path
                in = ClassLoader.getSystemResourceAsStream(resourcePath);
            }
        }
        return in;
    }

    private Class loadClass(ClassLoader cl, String className) {
        Class clazz = null;
        try {
            // load from custom class loader        
            clazz = cl.loadClass(className);
        } catch (ClassNotFoundException e) {
            try {
                // load from same class loader as class ElementMap
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e1) {
                // last resort: load from system class loader
                try {
                    clazz = ClassLoader.getSystemClassLoader().loadClass(className);
                } catch (ClassNotFoundException e2) {
                    logger.warn("missing class: " + e.getMessage());
                }
            }
        }
        return clazz;
    }
    private Map<String,List<String>> elements;

    public abstract Map addSpec(String value, Element element, Map map);

    public void dump(String format, Writer writer) throws IOException {
        Map<String,Object> m = map().get(format);
        if (m == null) {
            throw new IOException("format "+ format + " missing");
        }
        elements = newTreeMap();
        dump(null, m);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(writer, elements);
    }

    private void dump(String key, Map<String,Object> m) {
        for (String k : m.keySet()) {
            Object o = m.get(k);
            String kk = key == null ? k : key + "$" + k;
            if (o instanceof Map) {
                dump(kk, (Map) o);
            } else if (o instanceof Element) {
                Element e = (Element)o;
                String elemKey = e.getClass().getSimpleName();
                List<String> l = elements.get(elemKey);
                if (l == null) {
                    l = newLinkedList();
                }
                l.add(kk);
                elements.put(elemKey, l);
            }
        }
    }
}
