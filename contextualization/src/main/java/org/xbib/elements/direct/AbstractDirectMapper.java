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
package org.xbib.elements.direct;

import org.xbib.classloader.uri.URIClassLoader;
import org.xbib.keyvalue.KeyValue;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.context.ResourceContext;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Direct mapper is a mapper that is not using element mapping
 *
 * @param <K>
 * @param <V>
 * @param <C>
 */
public abstract class AbstractDirectMapper<K, V, C extends ResourceContext>
        implements DirectMapper<K,V,C> {

    private final Logger logger = LoggerFactory.getLogger(AbstractDirectMapper.class.getName());

    protected final BlockingQueue<List<KeyValue<K,V>>> queue;

    protected Set<KeyValueDirectPipeline> pipelines;

    private ExecutorService service;

    private LinkedList<KeyValue> keyvalues;

    private int numPipelines;

    public AbstractDirectMapper() {
        this(new URIClassLoader());
    }

    public AbstractDirectMapper(ClassLoader cl) {
        this.queue = new SynchronousQueue(true);
        this.pipelines = newHashSet();
    }

    @Override
    public DirectMapper<K,V,C> pipelines(int numPipelines) {
        this.numPipelines = numPipelines;
        return this;
    }

    @Override
    public Set<KeyValueDirectPipeline> pipelines() {
        return pipelines;
    }

    @Override
    public DirectMapper<K,V,C> start(DirectBuilderFactory<K,V,C> factory) {
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
        for (int i = 0; i < numPipelines; i++) {
            KeyValueDirectPipeline pipeline = createPipeline(i)
                    .setBuilder(factory.newBuilder());
            pipelines.add(pipeline);
            service.submit(pipeline);
        }
        logger.debug("starting direct mapper with {} pipelines", numPipelines);
        return this;
    }

    @Override
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
        logger.debug("closed direct mapper");
    }

    @Override
    public AbstractDirectMapper<K,V,C> begin() {
        keyvalues = newLinkedList();
        return this;
    }

    @Override
    public AbstractDirectMapper<K,V,C> keyValue(K key, V value) {
        keyvalues.add(new KeyValue(key, value));
        return this;
    }

    @Override
    public AbstractDirectMapper<K,V,C> keys(List<K> keys) {
        // unused
        return this;
    }

    @Override
    public AbstractDirectMapper<K,V,C> values(List<V> values) {
        // unused
        return this;
    }

    @Override
    public AbstractDirectMapper<K,V,C> end() {
        try {
            // move shallow copy of key/values to pipeline, this ensures thread safety
            queue.put((List<KeyValue<K,V>>) keyvalues.clone());
            keyvalues.clear();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return this;
    }

    protected abstract KeyValueDirectPipeline createPipeline(int i);

}
