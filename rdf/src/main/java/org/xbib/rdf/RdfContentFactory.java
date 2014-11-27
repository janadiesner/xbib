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
package org.xbib.rdf;

import org.xbib.rdf.io.ntriple.NTripleContent;
import org.xbib.rdf.io.ntriple.NTripleContentParams;
import org.xbib.rdf.io.rdfxml.RdfXmlContent;
import org.xbib.rdf.io.rdfxml.RdfXmlContentParams;
import org.xbib.rdf.io.turtle.TurtleContent;
import org.xbib.rdf.io.turtle.TurtleContentParams;
import org.xbib.rdf.io.xml.XmlContent;
import org.xbib.rdf.io.xml.XmlContentParams;
import org.xbib.rdf.memory.MemoryRdfGraph;

import java.io.IOException;
import java.io.OutputStream;

public class RdfContentFactory {

    public static RdfContentBuilder ntripleBuilder() throws IOException {
        return NTripleContent.contentBuilder(NTripleContentParams.DEFAULT_PARAMS);
    }

    public static RdfContentBuilder ntripleBuilder(NTripleContentParams params) throws IOException {
        return NTripleContent.contentBuilder(params);
    }

    public static RdfContentBuilder ntripleBuilder(OutputStream out) throws IOException {
        return NTripleContent.contentBuilder(out, NTripleContentParams.DEFAULT_PARAMS);
    }

    public static RdfContentBuilder ntripleBuilder(OutputStream out, NTripleContentParams params) throws IOException {
        return NTripleContent.contentBuilder(out, params);
    }

    public static RdfContentBuilder rdfXmlBuilder() throws IOException {
        return RdfXmlContent.contentBuilder(RdfXmlContentParams.DEFAULT_PARAMS);
    }

    public static RdfContentBuilder rdfXmlBuilder(RdfXmlContentParams params) throws IOException {
        return RdfXmlContent.contentBuilder(params);
    }

    public static RdfContentBuilder rdfXmlBuilder(OutputStream out) throws IOException {
        return RdfXmlContent.contentBuilder(out, RdfXmlContentParams.DEFAULT_PARAMS);
    }

    public static RdfContentBuilder rdfXmlBuilder(OutputStream out, RdfXmlContentParams params) throws IOException {
        return RdfXmlContent.contentBuilder(out, params);
    }

    public static RdfContentBuilder turtleBuilder() throws IOException {
        return TurtleContent.contentBuilder(TurtleContentParams.DEFAULT_PARAMS);
    }

    public static RdfContentBuilder turtleBuilder(TurtleContentParams params) throws IOException {
        return TurtleContent.contentBuilder(params);
    }

    public static RdfContentBuilder turtleBuilder(OutputStream out) throws IOException {
        return TurtleContent.contentBuilder(out, TurtleContentParams.DEFAULT_PARAMS);
    }

    public static RdfContentBuilder turtleBuilder(OutputStream out, TurtleContentParams params) throws IOException {
        return TurtleContent.contentBuilder(out, TurtleContentParams.DEFAULT_PARAMS);
    }

    public static RdfContentBuilder xmlBuilder() throws IOException {
        return rdfContentBuilder(StandardRdfContentType.XML, XmlContentParams.DEFAULT_PARAMS);
    }

    public static RdfContentBuilder xmlBuilder(XmlContentParams params) throws IOException {
        return rdfContentBuilder(StandardRdfContentType.XML, params);
    }

    public static RdfContentBuilder rdfContentBuilder(RdfContentType type, RdfContentParams params) throws IOException {
        if (type == StandardRdfContentType.NTRIPLE) {
            return NTripleContent.contentBuilder((NTripleContentParams) params);
        } else if (type == StandardRdfContentType.RDFXML) {
            return RdfXmlContent.contentBuilder((RdfXmlContentParams) params);
        } else if (type == StandardRdfContentType.TURTLE) {
            return TurtleContent.contentBuilder((TurtleContentParams) params);
        } else if (type == StandardRdfContentType.XML) {
            return XmlContent.contentBuilder((XmlContentParams) params);
        }
        throw new IllegalArgumentException("no content type for " + type);
    }

    public static <Params extends RdfGraphParams> RdfGraph<Params> memoryGraph() throws IOException {
        return new MemoryRdfGraph<Params>();
    }
}
