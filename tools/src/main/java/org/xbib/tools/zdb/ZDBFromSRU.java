
package org.xbib.tools.zdb;

import org.xbib.io.Connection;
import org.xbib.io.Session;
import org.xbib.io.StringPacket;
import org.xbib.io.archivers.tar.TarConnectionFactory;
import org.xbib.io.archivers.tar.TarSession;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.sru.client.SRUClient;
import org.xbib.sru.client.SRUClientFactory;
import org.xbib.sru.searchretrieve.SearchRetrieveRequest;
import org.xbib.sru.searchretrieve.SearchRetrieveResponse;
import org.xbib.tools.Converter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fetch SRU result from ZDB SRU service.
 * Output is archived as strings in a single TAR archive.
 */
public class ZDBFromSRU extends Converter {

    private final static Logger logger = LoggerFactory.getLogger(ZDBFromSRU.class.getName());

    private static Session<StringPacket> session;

    private final static AtomicLong counter = new AtomicLong();

    SRUClient client;

    public static void main(String[] args) {
        try {
            new ZDBFromSRU()
                    .reader(new InputStreamReader(System.in, "UTF-8"))
                    .writer(new OutputStreamWriter(System.out, "UTF-8"))
                    .run();
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            // close TAR
            if (session != null) {
                try {
                    session.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        System.exit(0);
    }

    private ZDBFromSRU() {
    }

    private ZDBFromSRU(boolean b) {
        client = SRUClientFactory.newClient();
    }

    protected ZDBFromSRU prepare() throws IOException {

        // open output TAR archive
        TarConnectionFactory factory = new TarConnectionFactory();
        Connection<TarSession> connection = factory.getConnection(URI.create(settings.get("output")));
        session = connection.createSession();
        if (session == null) {
            throw new IOException("can not open " + settings.get("output") + " for output");
        }
        session.open(Session.Mode.WRITE);

        // create input URLs
        input = new ConcurrentLinkedQueue<>();
        if (settings.get("numbers") != null) {
            FileInputStream in = new FileInputStream(settings.get("numbers"));
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = r.readLine()) != null) {
                input.add(URI.create(String.format(settings.get("uri"), line)));
            }
            in.close();
        } else {
            input.add(URI.create(settings.get("uri")));
        }
        logger.info("uris = {}", input.size());
        return this;
    }

    @Override
    protected PipelineProvider pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new ZDBFromSRU(true);
            }
        };
    }

    @Override
    protected void process(URI uri) throws Exception {
        StringWriter w = new StringWriter();
        SearchRetrieveRequest request = client.newSearchRetrieveRequest()
                .setURI(uri);
        SearchRetrieveResponse response = client.searchRetrieve(request).to(w);
        if (response.httpStatus() == 200 && w.toString().length() > 0) {
            StringPacket packet = new StringPacket();
            packet.name(Long.toString(counter.incrementAndGet()));
            packet.packet(w.toString());
            session.write(packet);
        }
    }

    protected ZDBFromSRU cleanup() {
        try {
            if (client != null) {
                client.close();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return this;
    }

}
