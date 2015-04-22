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

/**
 * Defines URIs for the RDF vocabulary terms and bnode constans used by framework.
 */
public interface RDF {

    String NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    String BNODE_PREFIX = "_:";
    String SHORTENABLE_BNODE_SUFFIX = "sbl";
    String PROPERTY = NS + "Property";
    String XML_LITERAL = NS + "XMLLiteral";
    String TYPE = NS + "type";
    String VALUE = NS + "value";
    String ALT = NS + "Alt";
    String BAG = NS + "Bag";
    String SEQ = NS + "Seq";
    String LIST = NS + "List";
    String FIRST = NS + "first";
    String NIL = NS + "nil";
    String REST = NS + "rest";
    String STATEMENT = NS + "Statement";
    String OBJECT = NS + "object";
    String PREDICATE = NS + "predicate";
    String SUBJECT = NS + "subject";
    String DESCRIPTION = NS + "Description";
    String ID = NS + "ID";
    String RDF = NS + "RDF";
    String ABOUT = NS + "about";
    String DATATYPE = NS + "datatype";
    String LI = NS + "li";
    String NODEID = NS + "nodeID";
    String PARSE_TYPE = NS + "parseType";
    String RESOURCE = NS + "resource";

}
