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
package org.xbib.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.metric.MeterMetric;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineRequest;
import org.xbib.pipeline.PipelineRequestListener;
import org.xbib.pipeline.element.PipelineElement;
import org.xbib.util.ExceptionFormatter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Maps.newHashMap;

public abstract class QueueConverterPipeline<T, R extends PipelineRequest, P extends Pipeline<T, R>, E extends PipelineElement>
        implements Pipeline<Boolean, R> {

    private final Logger logger;

    private QueueConverter<T, R, P, E> converter;

    private MeterMetric metric;

    private R request;

    private E element;

    private Map<String, PipelineRequestListener<Boolean, R>> listeners;

    public QueueConverterPipeline(QueueConverter<T, R, P, E> converter, int num) {
        this.converter = converter;
        this.listeners = newHashMap();
        this.logger = LogManager.getLogger(QueueConverterPipeline.class.getSimpleName() + "-pipeline-" + num);
    }

    public Pipeline<Boolean, R> add(String name, PipelineRequestListener<Boolean, R> listener) {
        listeners.put(name, listener);
        return this;
    }

    @Override
    public MeterMetric getMetric() {
        return metric;
    }

    @Override
    public Boolean call() throws Exception {
        logger.info("pipeline starting");
        try {
            metric = new MeterMetric(5L, TimeUnit.SECONDS);
            while (hasNext()) {
                request = next();
                process(request);
                for (PipelineRequestListener<Boolean, R> listener : listeners.values()) {
                    listener.newRequest(this, request);
                }
                metric.mark();
            }
        } catch (Throwable e) {
            logger.error("exception while processing {}, exiting", request);
            logger.error(ExceptionFormatter.format(e));
        } finally {
            converter.countDown();
            metric.stop();
        }
        logger.info("pipeline terminating");
        return true;
    }


    @Override
    public boolean hasNext() {
        try {
            element = converter.queue().poll(60, TimeUnit.SECONDS); // TODO make poll configurable
            if (element == null) {
                logger.warn("queue is empty?");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            element = null;
            logger.error("pipeline processing interrupted, queue inactive", e);
        }
        return element != null && element.get() != null;
    }

    @Override
    public abstract R next();

    @Override
    public void close() throws IOException {
        logger.info("closing");
    }

    protected E getElement() {
        return element;
    }

    protected abstract void process(R request) throws IOException;
}
