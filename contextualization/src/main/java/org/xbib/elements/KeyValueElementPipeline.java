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

import org.xbib.keyvalue.KeyValue;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.event.EventListener;
import org.xbib.marc.event.FieldEvent;
import org.xbib.rdf.Context;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.BlockingQueue;

/**
 * A key/value pipeline for threaded processing of key/value streams
 *
 * @param <K>
 * @param <V>
 * @param <E>
 * @param <C>
 */
public class KeyValueElementPipeline<K,V,E extends Element,C extends Context>
        implements Callable<Boolean> {

    protected Specification specification;

    private BlockingQueue<List<KeyValue<K,V>>> queue;

    private ElementBuilder<K,V,E,C> builder;

    private Map map;

    private final Logger logger;

    private UnmappedKeyListener<K> listener;

    public KeyValueElementPipeline(int i) {
        this.logger = LoggerFactory.getLogger("pipeline" + i);
    }

    public KeyValueElementPipeline<K,V,E,C> setSpecification(Specification specification) {
        this.specification = specification;
        return this;
    }

    public Specification getSpecification() {
        return specification;
    }

    public KeyValueElementPipeline<K,V,E,C> setMap(Map map) {
        this.map = map;
        return this;
    }

    public Map getMap() {
        return map;
    }

    public KeyValueElementPipeline<K,V,E,C> setQueue(BlockingQueue<List<KeyValue<K,V>>> queue) {
        this.queue = queue;
        return this;
    }

    public BlockingQueue<List<KeyValue<K,V>>> getQueue() {
        return queue;
    }

    public KeyValueElementPipeline<K,V,E,C> setElementBuilder(ElementBuilder<K,V,E,C> builder) {
        this.builder = builder;
        return this;
    }

    @Override
    public Boolean call() {
        try {
            logger.debug("key/value pipeline {} starting", getClass().getName());
            while(true) {
                List<KeyValue<K,V>> keyvalueList = queue.take();
                // if poison element then quit
                if (keyvalueList.isEmpty()) {
                    logger.debug("key/value pipeline {} ending", getClass().getName());
                    break;
                }
                // if only a single element in list with empty key then skip this element
                if (keyvalueList.size() == 1) {
                    if (keyvalueList.get(0).key() == null) {
                        logger.debug("single marker element skipped");
                        continue;
                    }
                }
                builder.begin();
                boolean end = false;
                for (KeyValue<K,V> kv : keyvalueList) {
                    K key = kv.key();
                    V value = kv.value();
                    if (key == null) {
                        builder.end();
                        end = true;
                    } else {
                        build(key, value);
                    }
                }
                if (!end) {
                    builder.end();
                }
            }
        } catch (InterruptedException ex) {
            logger.warn("key/value pipeline {} interrupted", getClass());
            Thread.currentThread().interrupt();
        } catch (Throwable t) {
            logger.error("error in key/value pipeline, exiting", t);
        }
        return true;
    }

    protected ElementBuilder<K,V,E,C> builder() {
        return builder;
    }

    protected void build(K key, V value) {
    }

    public KeyValueElementPipeline<K,V,E,C> setListener(UnmappedKeyListener<K> listener) {
        this.listener = listener;
        return this;
    }

    public UnmappedKeyListener<K> getListener() {
        return listener;
    }

}
