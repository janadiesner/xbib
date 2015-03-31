/**
 * Copyright 2012-2013 the Semargl contributors. See AUTHORS for more details.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.semarglproject.vocab;

/**
 * Defines URIs for the RDF vocabulary terms and bnode constans used by framework.
 */
public interface RDF {

    String BNODE_PREFIX = "_:";

    // indicates that short bnode syntax shouldn't be used for this node
    String SHORTENABLE_BNODE_SUFFIX = "sbl";

    String NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    // Basic classes and properties

    String PROPERTY = NS + "Property";
    String XML_LITERAL = NS + "XMLLiteral";

    String TYPE = NS + "type";
    String VALUE = NS + "value";

    // Container and collection classes and properties

    String ALT = NS + "Alt";
    String BAG = NS + "Bag";
    String SEQ = NS + "Seq";
    String LIST = NS + "List";

    String FIRST = NS + "first";
    String NIL = NS + "nil";
    String REST = NS + "rest";

    // Reification

    String STATEMENT = NS + "Statement";

    String OBJECT = NS + "object";
    String PREDICATE = NS + "predicate";
    String SUBJECT = NS + "subject";

    // Syntax names

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
