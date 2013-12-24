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
package org.xbib.marc.dialects;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import org.xbib.elements.CountableElementOutput;
import org.xbib.elements.marc.dialects.mab.MABElementBuilder;
import org.xbib.elements.marc.dialects.mab.MABContext;
import org.xbib.elements.marc.dialects.mab.MABElementBuilderFactory;
import org.xbib.elements.marc.dialects.mab.MABElementMapper;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.SimplePipelineExecutor;
import org.xbib.pipeline.Pipeline;
import org.xbib.rdf.Resource;
import org.xbib.rdf.xcontent.ContentBuilder;
import org.xbib.util.IntervalIterator;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.MarcXchange2KeyValue;

public class ConcurrentAlephPublishingReaderTest {

    private final static Logger logger = LoggerFactory.getLogger(AlephPublishingReader.class.getName());

    private final Iterator<Long> iterator = new IntervalIterator(1, 100);

    private final int threads = 4;

    private String library;

    private String setName;

    public void testAlephPublishing() throws InterruptedException, ExecutionException {
        //System.setProperty("java.naming.factory.initial", "org.xbib.naming.SimpleContextFactory");

        ResourceBundle bundle = ResourceBundle.getBundle("org.xbib.marc.extensions.alephtest");
        library  = bundle.getString("library");
        setName = bundle.getString("setname");
        String uriStr = bundle.getString("uri");


        Queue<URI> uris = new LinkedList();

        for (int i = 0; i < threads; i++) {
            uris.add(URI.create(uriStr));
        }
        SimplePipelineExecutor service = new SimplePipelineExecutor()
                .concurrency(threads)
                .provider(new PipelineProvider() {

                    @Override
                    public Pipeline get() {
                        return createPipeline();
                    }
                })
                .execute();
    }

    private Pipeline createPipeline() {
        final CountableElementOutput<MABContext,Resource> output = new CountableElementOutput<MABContext,Resource>() {
            @Override
            public void output(MABContext context, ContentBuilder contentBuilder) throws IOException {
                counter.incrementAndGet();
            }
        };
        final MABElementBuilderFactory builderFactory = new MABElementBuilderFactory() {
            public MABElementBuilder newBuilder() {
                return new MABElementBuilder().addOutput(output);
            }
        };
        final MABElementMapper mapper = new MABElementMapper("mab").start(builderFactory);
        final MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(mapper);
        return new AlephPublishingReader()
                .setListener(kv)
                .setIterator(iterator)
                .setLibrary(library)
                .setSetName(setName);
    }
}
