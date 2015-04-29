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


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A pool of precompiled XSLT stylesheets ({@link javax.xml.transform.Templates}).
 */
public final class StylesheetPool {

    private final static Logger logger = LogManager.getLogger(StylesheetPool.class.getName());

    /**
     * A map of precompiled stylesheets ({@link javax.xml.transform.Templates} objects).
     */
    private final Map<String, Templates> stylesheets = new ConcurrentHashMap();

    /**
     * @return returns the identity transformer handler.
     */
    public TransformerHandler getIdentityTransformerHandler(SAXTransformerFactory transformerFactory)
            throws TransformerConfigurationException {
        return transformerFactory.newTransformerHandler();
    }

    public boolean hasTemplate(StreamSource source) {
        return stylesheets.containsKey(source.getSystemId());
    }

    /**
     * Retrieves a previously stored template, if available.
     */
    public Templates getTemplate(String systemId) {
        return stylesheets.get(systemId);
    }

    /**
     * Create a template, add to the pool if necessary. Addition is quite costly
     * as it replaces the internal {@link #stylesheets} {@link java.util.HashMap}.
     */
    public Templates newTemplates(SAXTransformerFactory transformerFactory, Source source)
            throws TransformerConfigurationException {
        String systemId = source.getSystemId();
        Templates template = stylesheets.get(systemId);
        if (template == null) {
            logger.debug("new source={} {}", source.getSystemId(), source.getClass().getName());
            template = transformerFactory.newTemplates(source);
            stylesheets.put(systemId, template);
        }
        return template;
    }

    /**
     * Return a new {@link javax.xml.transform.sax.TransformerHandler} based on a given precompiled
     * {@link javax.xml.transform.Templates}.
     */
    public TransformerHandler newTransformerHandler(SAXTransformerFactory transformerFactory, Templates template)
            throws TransformerConfigurationException {
        final TransformerHandler handler = transformerFactory.newTransformerHandler(template);
        /*
         * We want to raise transformer exceptions on <xml:message terminate="true">, so
         * we add a custom listener. Also, various XSLT processors react in different ways
         * to transformation errors -- some of them report error as recoverable, some of
         * them report error as unrecoverable.
         */
        handler.getTransformer().setErrorListener(new TransformerErrorListener());
        return handler;
    }
}
