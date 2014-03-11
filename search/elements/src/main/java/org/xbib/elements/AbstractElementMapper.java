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

import org.xbib.classloader.uri.URIClassLoader;
import org.xbib.io.keyvalue.KeyValue;
import org.xbib.io.keyvalue.KeyValueStreamListener;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.context.ResourceContext;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

/**
 * Basic element mapper
 *
 * @param <K>
 * @param <V>
 * @param <E>
 * @param <C>
 */
public abstract class AbstractElementMapper<K, V, E extends Element, C extends ResourceContext>
        implements KeyValueStreamListener<K, V>, Closeable {

    private final Logger logger = LoggerFactory.getLogger(AbstractElementMapper.class.getName());

    protected Specification specification;

    protected final BlockingQueue<List<KeyValue>> queue;

    protected final Map map;

    protected ElementBuilderFactory<K, V, E, C> factory;

    protected Set<KeyValuePipeline> pipelines;

    private ExecutorService service;

    private LinkedList<KeyValue> keyvalues;

    private int numPipelines;

    private boolean detectUnknownKeys;

    public AbstractElementMapper(String path, String format, AbstractSpecification specification) {
        this(new URIClassLoader(), path, format, specification);
    }

    public AbstractElementMapper(ClassLoader cl, String path, String format, AbstractSpecification specification) {
        this.specification = specification;
        this.queue = new SynchronousQueue<>(true);
        this.pipelines = new HashSet();
        try {
            this.map = specification.getElementMap(cl, path, format);
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public Map map() {
        return map;
    }

    public AbstractElementMapper pipelines(int numPipelines) {
        this.numPipelines = numPipelines;
        return this;
    }

    public AbstractElementMapper detectUnknownKeys(boolean enabled) {
        this.detectUnknownKeys = enabled;
        return this;
    }

    public Set<KeyValuePipeline> pipelines() {
        return pipelines;
    }

    public AbstractElementMapper start(ElementBuilderFactory factory) {
        if (numPipelines == 0) {
            numPipelines = 1;
        }
        // restrict numPipelines to a reasonable value
        if (numPipelines >= 256) {
            numPipelines = 256;
        }
        if (service == null) {
            this.service = Executors.newFixedThreadPool(numPipelines);
        }
        this.factory = factory;
        for (int i = 0; i < numPipelines; i++) {
            KeyValuePipeline pipeline = createPipeline(i)
                    .detectUnknownKeys(detectUnknownKeys);
            pipelines.add(pipeline);
            service.submit(pipeline);
        }
        logger.debug("starting element mapper with {} pipelines", numPipelines);
        return this;
    }

    public void close() {
        if (service == null) {
            return;
        }
        for (int i = 0; i < numPipelines; i++) {
            try {
                queue.put(new LinkedList()); // send poison element to all numPipelines
            } catch (InterruptedException e) {
                logger.error("interrupted while close()");
            }
        }
        service.shutdownNow();
        logger.debug("closed element mapper");
    }

    @Override
    public AbstractElementMapper<K,V,E,C> begin() {
        keyvalues = new LinkedList();
        return this;
    }

    @Override
    public AbstractElementMapper<K,V,E,C>  keyValue(K key, V value) {
        keyvalues.add(new KeyValue(key, value));
        return this;
    }

    @Override
    public AbstractElementMapper<K,V,E,C>  keys(List<K> keys) {
        // unused
        return this;
    }

    @Override
    public AbstractElementMapper<K,V,E,C>  values(List<V> values) {
        // unused
        return this;
    }

    @Override
    public AbstractElementMapper<K,V,E,C>  end() {
        try {
            // move shallow copy of key/values to pipeline, this ensures thread safety
            queue.put((List<KeyValue>) keyvalues.clone());
            keyvalues.clear();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return this;
    }

    /**
     * Helper method for diagnosing unknown keys.
     *
     * @return a printable set of unknown keys
     */
    public Set<String> unknownKeys() {
        Set<String> unknownKeys = new TreeSet<String>();
        for (KeyValuePipeline p : pipelines()) {
            for (Object s : p.unknownKeys) {
                unknownKeys.add("\"" + s + "\"");
            }
        }
        return unknownKeys;
    }

    public void dump(String format, Writer writer) throws IOException {
        specification.dump(format, writer);
    }

    protected abstract KeyValuePipeline createPipeline(int i);

}
