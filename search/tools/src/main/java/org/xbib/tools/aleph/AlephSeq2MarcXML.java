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
package org.xbib.tools.aleph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.Pipeline;
import org.xbib.io.progress.BytesProgressWatcher;
import org.xbib.io.InputService;
import org.xbib.io.progress.ProgressMonitoredOutputStream;
import org.xbib.io.SplitWriter;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchangeAdapter;
import org.xbib.marc.dialects.AlephSequentialReader;
import org.xbib.pipeline.element.CounterElement;
import org.xbib.tools.Converter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class AlephSeq2MarcXML extends Converter {

    private final static Logger logger = LoggerFactory.getLogger(AlephSeq2MarcXML.class.getName());

    private final static CounterElement fileCounter = new CounterElement().set(new AtomicLong(0L));

    private final int BUFFER_SIZE = 8192;

    private final static long splitsize = 1000000L;

    private final static String linkformat = "http://index.hbz-nrw.de/query/services/document/xhtml/hbz/title/%s";

    private BytesProgressWatcher watcher;

    public static void main(String[] args) {
        try {
            new AlephSeq2MarcXML()
                    .reader(new InputStreamReader(System.in, "UTF-8"))
                    .writer(new OutputStreamWriter(System.out, "UTF-8"))
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private AlephSeq2MarcXML() {
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new AlephSeq2MarcXML();
            }
        };
    }

    @Override
    protected Converter prepare() {
        super.prepare();
        watcher = new BytesProgressWatcher(BUFFER_SIZE);
        return this;
    }

    @Override
    protected Converter cleanup() {
        super.cleanup();
        return this;
    }

    @Override
    public void process(URI uri) throws Exception {
        InputStream in = InputService.getInputStream(uri);
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        AlephSequentialReader seq = new AlephSequentialReader(br);
        try (SplitWriter bw = new SplitWriter(newWriter(watcher), BUFFER_SIZE)) {
            final Iso2709Reader reader = new Iso2709Reader();
            final StreamResult target = new StreamResult(bw);
            reader.setMarcXchangeListener(new MarcXchangeAdapter() {

                @Override
                public void endRecord() {
                    Integer n =  settings.getAsInt("941", 941);
                    Field f1 = new Field("941", "  ");
                    f1.subfieldId("d");
                    reader.getAdapter().beginDataField(f1);
                    reader.getAdapter().beginSubField(f1);
                    f1.data("1");
                    reader.getAdapter().endSubField(f1);
                    reader.getAdapter().endDataField(null);
                    Integer m = settings.getAsInt("956", 956);
                    Field f2 = new Field("956", "  ");
                    f2.subfieldId("u");
                    reader.getAdapter().beginDataField(f2);
                    reader.getAdapter().beginSubField(f2);
                    String p = String.format(linkformat, reader.getAdapter().getIdentifier());
                    f2.data(p);
                    reader.getAdapter().endSubField(f2);
                    reader.getAdapter().endDataField(null);
                    if (watcher.getBytesTransferred() > splitsize) {
                        try {
                            reader.getAdapter().endCollection();
                            target.getWriter().flush();
                            bw.split(newWriter(watcher));
                            reader.getAdapter().beginCollection();
                            watcher.resetWatcher();
                        } catch (IOException | SAXException ex) {
                            logger.error(ex.getMessage(), ex);
                        }
                    }
                }

            });
            reader.setProperty(Iso2709Reader.SCHEMA, "marc21");
            reader.setProperty(Iso2709Reader.FORMAT, "Marc21");
            reader.setProperty(Iso2709Reader.TYPE, "Bibliographic");
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.transform(new SAXSource(reader, new InputSource(seq)), target);
        }
    }

    private Writer newWriter(BytesProgressWatcher watcher) throws IOException {
        File dir = new File(settings.get("output"));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!dir.isDirectory() && !dir.canWrite()) {
            throw new IOException("unable to write to directory " + settings.get("output"));
        }
        String filename = dir + File.separator + settings.get("basename") + "_" + fileCounter.get().getAndIncrement() + ".xml";
        OutputStream out = new ProgressMonitoredOutputStream(new FileOutputStream(filename), watcher);
        return new OutputStreamWriter(out, "UTF-8");
    }
}
