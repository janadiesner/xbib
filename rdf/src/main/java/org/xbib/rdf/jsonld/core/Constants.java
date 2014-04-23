package org.xbib.rdf.jsonld.core;

/**
 * URI Constants used in the JSON-LD parser.
 */
public interface Constants {

    String RDF_SYNTAX_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    String RDF_SCHEMA_NS = "http://www.w3.org/2000/01/rdf-schema#";
    String XSD_NS = "http://www.w3.org/2001/XMLSchema#";

    String XSD_ANYTYPE = XSD_NS + "anyType";
    String XSD_BOOLEAN = XSD_NS + "boolean";
    String XSD_DOUBLE = XSD_NS + "double";
    String XSD_INTEGER = XSD_NS + "integer";
    String XSD_FLOAT = XSD_NS + "float";
    String XSD_DECIMAL = XSD_NS + "decimal";
    String XSD_ANYURI = XSD_NS + "anyURI";
    String XSD_STRING = XSD_NS + "string";

    String RDF_TYPE = RDF_SYNTAX_NS + "type";
    String RDF_FIRST = RDF_SYNTAX_NS + "first";
    String RDF_REST = RDF_SYNTAX_NS + "rest";
    String RDF_NIL = RDF_SYNTAX_NS + "nil";
    String RDF_PLAIN_LITERAL = RDF_SYNTAX_NS + "PlainLiteral";
    String RDF_XML_LITERAL = RDF_SYNTAX_NS + "XMLLiteral";
    String RDF_OBJECT = RDF_SYNTAX_NS + "object";
    String RDF_LANGSTRING = RDF_SYNTAX_NS + "langString";
    String RDF_LIST = RDF_SYNTAX_NS + "List";
}
