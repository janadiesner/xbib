package org.xbib.stream;

import org.xbib.stream.dsl.Streams;
import org.xbib.stream.generators.Generator;
import org.xbib.stream.generators.NoOpGenerator;
import org.xbib.stream.generators.Processor;

import java.net.URI;

import static org.xbib.stream.dsl.Streams.convert;
import static org.xbib.stream.dsl.Streams.guard;
import static org.xbib.stream.dsl.Streams.pipe;

public class StreamTests {

    public void testStream() {
        /*MyElementService service = null;
        URI idRs = URI.create("null://localhos");
        URI elementRs =  service.lookup(idRs);
        Stream<MyElement> elements = convert(elementRs).;

        elements = guard(elements).with(Streams.IGNORE_POLICY);
        Processor<MyElement> updater = new Processor<MyElement>() {
            @Override
            protected void process(MyElement element) {
                // do nothing
            }
        };
        Stream<MyElement> updated = pipe(elements).through(updater);

        updated = guard(updated).with(Streams.STOPFAST_POLICY);
        Generator<MyElement,String> serialiser = new NoOpGenerator<>();
        URI updatedRS = publish(updated).using(serialiser).withDefaults();
        URI outcomeRs = service.update(updatedRS);

        Stream<Result> results = convert(outcomeRs).ofStrings().withDefaults();

        try {
            while (results.hasNext())
                results.next();
        }
        finally {
            results.close();
        }
        */
    }

    class MyElementService {

        URI lookup(URI uri) {
            return uri;
        }

        URI update(URI uri) {
            return uri;
        }
    }

    class MyElement {

    }

    class Result {

    }
}
