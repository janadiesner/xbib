package org.xbib.rdf.io.nquads;

import org.xbib.rdf.io.ntriple.NTriplesSerializer;
import org.xbib.rdf.io.sink.CharSink;
import org.xbib.rdf.io.sink.QuadSink;

import java.io.IOException;

/**
 * Implementation of {@link org.xbib.rdf.io.sink.TripleSink} which serializes triples to
 * {@link org.xbib.rdf.io.sink.CharSink} using <a href="">NTriples</a> syntax. *
 */
public class NQuadsSerializer extends NTriplesSerializer implements QuadSink {

    private final CharSink sink;

    private NQuadsSerializer(CharSink sink) {
        super(sink);
        this.sink = sink;
    }

    /**
     * Creates instance of TurtleSerializer connected to specified sink.
     *
     * @param sink sink to be connected to
     * @return instance of TurtleSerializer
     */
    public static QuadSink connect(CharSink sink) {
        return new NQuadsSerializer(sink);
    }

    @Override
    public void addNonLiteral(String subj, String pred, String obj, String graph) {
        try {
            startTriple(subj, pred);
            serializeBnodeOrUri(obj);
            if (graph != null) {
                serializeBnodeOrUri(graph);
            }
            sink.process(DOT_EOL);
        } catch (IOException e) {
            // ignore
        }
    }

    @Override
    public void addPlainLiteral(String subj, String pred, String content, String lang, String graph) {
        try {
            startTriple(subj, pred);
            addContent(content);
            if (lang != null) {
                sink.process('@').process(lang);
            }
            sink.process(SPACE);
            if (graph != null) {
                serializeBnodeOrUri(graph);
            }
            sink.process(DOT_EOL);
        } catch (IOException e) {
            // ignore
        }
    }

    @Override
    public void addTypedLiteral(String subj, String pred, String content, String type, String graph) {
        try {
            startTriple(subj, pred);
            addContent(content);
            sink.process("^^");
            serializeUri(type);
            if (graph != null) {
                serializeBnodeOrUri(graph);
            }
            sink.process(DOT_EOL);
        } catch (IOException e) {
            // ignore
        }
    }

}
