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

import java.io.IOException;
import java.util.List;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;

import static com.google.common.collect.Lists.newLinkedList;

/**
 * Base class for all element builders
 *
 * @param <K> the key type
 * @param <V> the value type
 * @param <E> the element type
 * @param <C> the resource context type
 */
public abstract class AbstractElementBuilder<K, V, E extends Element, C extends ResourceContext<Resource>>
        implements ElementBuilder<K, V, E, C> {

    private final static Logger logger = LoggerFactory.getLogger(AbstractElementBuilder.class.getName());

    private final ThreadLocal<C> contexts = new ThreadLocal<C>();

    private final List<ContextResourceOutput> outputs = newLinkedList();

    @Override
    public void build(E element, K key, V value) {
    }

    @Override
    public void begin() {
        C context = contextFactory().newContext();
        context.setResource(context.newResource());
        contexts.set(context);
    }

    @Override
    public void end() {
        C context = context();
        context.beforeOutput();
        for (ContextResourceOutput output : outputs) {
            try {
               output.output(context, context.getResource(), context.getContentBuilder());
            } catch (IOException e) {
                logger.error("output failed: " + e.getMessage(), e);
            }
        }
        context.afterOutput();
    }

    @Override
    public C context() {
        return contexts.get();
    }

    @Override
    public AbstractElementBuilder<K, V, E, C> addOutput(ContextResourceOutput output) {
        outputs.add(output);
        return this;
    }

}
