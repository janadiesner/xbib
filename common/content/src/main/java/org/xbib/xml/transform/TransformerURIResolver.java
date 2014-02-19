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
package org.xbib.xml.transform;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;

/**
 * URI resolver for Transformer
 */
public class TransformerURIResolver implements URIResolver {

    private final Logger logger = LoggerFactory.getLogger(TransformerURIResolver.class.getName());

    private List<InputStream> inputStreams = newLinkedList();

    private List<String> bases = newLinkedList();

    private ClassLoader classLoader;

    public TransformerURIResolver() {
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    public TransformerURIResolver(String... bases) {
        this.classLoader = Thread.currentThread().getContextClassLoader();
        this.bases.addAll(Arrays.asList(bases));
    }

    public TransformerURIResolver setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    /**
     * @param href An href attribute, which may be relative or absolute
     * @param base The base URI against which the first argument will be made absolute if the absolute URI is required
     * @return the souce
     * @throws TransformerException
     */
    @Override
    public Source resolve(String href, String base) throws TransformerException {
        InputStream in = null;
        URL url = null;
        try {
            URI uri = URI.create(href);
            // relative href?
            if (!uri.isAbsolute() && base != null) {
                url = new URL(new URL(base), href);
                href = url.toURI().getRawSchemeSpecificPart(); // drop scheme
            }
        } catch (MalformedURLException | URISyntaxException e) {
            throw new TransformerException(e);
        }
        String systemId = href;
        if (url != null) {
            try {
                in = url.openStream();
            } catch (IOException e) {
                logger.debug("error while opening stream", e);
            }
        }
        if (in == null) {
            try {
                url = classLoader.getResource(href);
                if (url != null) {
                    systemId = url.toExternalForm();
                    in = url.openStream();
                } else {
                    systemId = href;
                    in = classLoader.getResourceAsStream(href);
                    if (in == null) {
                        if (bases.isEmpty()) {
                            try {
                                systemId = href;
                                in = new FileInputStream(href);
                            } catch (FileNotFoundException e) {
                                logger.debug("file not found: " + href);
                            }
                        } else {
                            for (String s : bases) {
                                systemId = s + "/" + href;
                                in = classLoader.getResourceAsStream(systemId);
                                if (in == null) {
                                    try {
                                        in = new FileInputStream(systemId);
                                    } catch (FileNotFoundException e) {
                                        logger.debug("file not found: " + systemId);
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new TransformerException("I/O error", e);
            }
        }
        if (in == null) {
            throw new TransformerException("href could not be resolved: " + href);
        }
        try {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            inputStreams.add(in);
            SAXSource source = new SAXSource(reader, new InputSource(in));
            source.setSystemId(systemId);
            return source;
        } catch (SAXException e) {
            throw new TransformerException("no XML reader for SAX source in URI resolving for:" + href, e);
        }
    }

    public void close() throws IOException {
        for (InputStream in : inputStreams) {
            in.close();
        }
    }
}
