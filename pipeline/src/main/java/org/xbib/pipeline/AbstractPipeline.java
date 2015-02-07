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
package org.xbib.pipeline;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Basic pipeline for pressing pipeline requests.
 * This abstract class can be used for creating custom Pipeline classes.
 *
 * @param <R> the pipeline request type
 * @param <E> the pipeline error type
 */
public abstract class AbstractPipeline<R extends PipelineRequest, E extends PipelineException>
        implements Pipeline<Boolean,R>, PipelineRequestListener<Boolean,R>,
        PipelineErrorListener<Boolean,R,E> {

    private String name;

    private Integer number;

    /**
     * A list of request listeners for processing requests
     */
    private Map<String,PipelineRequestListener<Boolean,R>> requestListeners =
            new LinkedHashMap<String,PipelineRequestListener<Boolean,R>>();

    /**
     * A list of error listeners for processing errors
     */
    private Map<String,PipelineErrorListener<Boolean,R,E>> errorListeners =
            new LinkedHashMap<String,PipelineErrorListener<Boolean,R,E>>();

    public AbstractPipeline<R,E> setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public AbstractPipeline<R,E> setNumber(Integer number) {
        this.number = number;
        return this;
    }

    public Integer getNumber() {
        return number;
    }

    /**
     * Add a pipeline request listener to the pipeline. The listener is called each time
     * this pipeline processes a new request.
     * @param name the listener name
     * @param listener the listener
     * @return this pipeline
     */
    public Pipeline<Boolean,R> add(String name, PipelineRequestListener<Boolean,R> listener) {
        if (name != null) {
            this.requestListeners.put(name,listener);
        }
        return this;
    }

    /**
     * Add a pipeline request listener to the pipeline. The listener is called each time
     * this pipeline processes a new request.
     * @param name the listener name
     * @param listener the listener
     * @return this pipeline
     */
    public Pipeline<Boolean,R> add(String name, PipelineErrorListener<Boolean,R,E> listener) {
        if (name != null) {
            this.errorListeners.put(name,listener);
        }
        return this;
    }

    /**
     * Call this thread. Iterate over all request and pass them to request listeners.
     * At least, this pipeline itself can listen to requests and handle errors.
     * Only PipelineExceptions are handled for each listener. Other execptions will quit the
     * pipeline request executions.
     * @return a metric about the pipeline request executions.
     * @throws Exception if pipeline execution was sborted by a non-PipelineException
     */
    @Override
    public Boolean call() throws Exception {
        Iterator<R> it = this;
        while (it.hasNext()) {
            R r = it.next();
            // add ourselves if not already done
            requestListeners.put(null, this);
            errorListeners.put(null, this);
            for (PipelineRequestListener<Boolean,R> requestListener : requestListeners.values()) {
                try {
                    requestListener.newRequest(this, r);
                } catch (PipelineException e) {
                    for (PipelineErrorListener<Boolean,R,E> errorListener : errorListeners.values()) {
                        errorListener.error(this, r, (E) e);
                    }
                }
            }
        }
        close();
        return true;
    }

    /**
     * Removing pipeline requests is not supported.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove not supported");
    }

    /**
     * A new request for the pipeline is processed.
     * @param pipeline the pipeline
     * @param request the pipeline request
     */
    public abstract void newRequest(Pipeline<Boolean,R> pipeline, R request);

    /**
     * A PipelineException occured.
     * @param pipeline the pipeline
     * @param request the pipeline request
     * @param error the pipeline error
     */
    public abstract void error(Pipeline<Boolean,R> pipeline, R request, E error);

}
