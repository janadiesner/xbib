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

import org.xbib.keyvalue.KeyValue;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.context.ResourceContext;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * A key/value pipeline for threaded direct processing
 *
 * @param <K>
 * @param <V>
 * @param <C>
 */
public class KeyValueDirectPipeline<K,V,C extends ResourceContext>
        implements Callable<Boolean> {

    private BlockingQueue<List<KeyValue<K,V>>> queue;


    private DirectBuilder<K,V,C> builder;

    private final Logger logger;

    private long counter;

    public KeyValueDirectPipeline(int i) {
        this.logger = LoggerFactory.getLogger("pipeline" + i);
        this.counter = 0L;
    }

    public KeyValueDirectPipeline setQueue(BlockingQueue<List<KeyValue<K,V>>> queue) {
        this.queue = queue;
        return this;
    }

    public BlockingQueue<List<KeyValue<K,V>>> getQueue() {
        return queue;
    }

    public KeyValueDirectPipeline setBuilder(DirectBuilder<K,V,C> builder) {
        this.builder = builder;
        return this;
    }

    @Override
    public Boolean call() {
        try {
            logger.debug("starting");
            while(true) {
                List<KeyValue<K,V>> keyvalueList = queue.take();
                // if poison element then quit
                if (keyvalueList.isEmpty()) {
                    logger.debug("ending");
                    break;
                }
                // if only a single element in list and null key, then skip this element
                if (keyvalueList.size() == 1 && keyvalueList.get(0).key() == null) {
                    logger.debug("marker skipped");
                    continue;
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
                    counter++;
                }
                if (!end) {
                    builder.end();
                }
            }
        } catch (InterruptedException ex) {
            logger.warn("interrupted");
            Thread.currentThread().interrupt();
        } catch (Throwable t) {
            logger.error("error in pipeline, exiting", t);
        }
        return true;
    }

    public long getCounter() {
        return counter;
    }

    protected DirectBuilder<K,V,C> builder() {
        return builder;
    }

    protected void build(K key, V value) {
    }

}
