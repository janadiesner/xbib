package org.xbib.rdf.jsonld.core;

public interface JSONLDTripleCallback {

    /**
     * Construct output based on internal RDF dataset format
     *
     * @param dataset The format of the dataset is a Map with the following
     *                structure: { GRAPH_1: [ TRIPLE_1, TRIPLE_2, ..., TRIPLE_N ],
     *                GRAPH_2: [ TRIPLE_1, TRIPLE_2, ..., TRIPLE_N ], ... GRAPH_N: [
     *                TRIPLE_1, TRIPLE_2, ..., TRIPLE_N ] }
     *                <p>
     *                GRAPH: Is the graph name/IRI. if no graph is present for a
     *                triple, it will be listed under the "@default" graph TRIPLE:
     *                Is a map with the following structure: { "subject" : SUBJECT
     *                "predicate" : PREDICATE "object" : OBJECT }
     *                <p>
     *                Each of the values in the triple map are also maps with the
     *                following key-value pairs: "value" : The value of the node.
     *                "subject" can be an IRI or blank node id. "predicate" should
     *                only ever be an IRI "object" can be and IRI or blank node id,
     *                or a literal value (represented as a string) "type" : "IRI" if
     *                the value is an IRI or "blank node" if the value is a blank
     *                node. "object" can also be "literal" in the case of literals.
     *                The value of "object" can also contain the following optional
     *                key-value pairs: "language" : the language value of a string
     *                literal "datatype" : the datatype of the literal. (if not set
     *                will default to XSD:string, if set to null, null will be
     *                used).
     * @return the resulting RDF object in the desired format
     */
    public Object call(RDFDataset dataset);
}
