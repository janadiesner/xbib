package org.xbib.io.archive.elasticsearch.bulk;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.VersionType;
import org.xbib.io.archivers.ArchiveEntry;
import org.xbib.io.archivers.ArchiveInputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class BulkArchiveInputStream extends ArchiveInputStream {

    private final static Charset UTF8 = Charset.forName("UTF-8");

    private BulkArchiveEntry entry;

    private BufferedReader reader;

    private boolean entryEOF = false;

    private boolean closed = false;


    int from = 0;


    public BulkArchiveInputStream(InputStream in) {
        this.reader = new BufferedReader(new InputStreamReader(in, UTF8));
    }

    @Override
    public ArchiveEntry getNextEntry() throws IOException {
        return getNextBulkEntry();
    }
    @Override
    public void close() throws IOException {
        if (!this.closed) {
            reader.close();
            this.closed = true;
        }
    }

    private void closeEntry() throws IOException {
        ensureOpen();
        this.entryEOF = true;
    }

    /**
     * Check to make sure that this stream has not been closed
     *
     * @throws java.io.IOException if the stream is already closed
     */
    private void ensureOpen() throws IOException {
        if (this.closed) {
            throw new IOException("stream closed");
        }
    }

    public BulkArchiveEntry getNextBulkEntry() throws IOException {
        ensureOpen();
        if (this.entry != null) {
            closeEntry();
        }
        BulkArchiveEntry entry = new BulkArchiveEntry();
        String s1 = reader.readLine();
        BytesArray b = new BytesArray(s1);

        return entry;
    }

    private IndexRequest parse(BytesReference data) throws IOException {
        IndexRequest request = null;
        XContent xContent = XContentFactory.xContent(data);
        int length = data.length();
        byte marker = xContent.streamSeparator();
        int nextMarker = findNextMarker(marker, from, data, length);
        if (nextMarker == -1) {
            return request;
        }
        XContentParser parser = xContent.createParser(data.slice(from, nextMarker - from));
        try {
            from = nextMarker + 1;
            XContentParser.Token token = parser.nextToken();
            if (token == null) {
                return request;
            }
            assert token == XContentParser.Token.START_OBJECT;
            token = parser.nextToken();
            assert token == XContentParser.Token.FIELD_NAME;
            String action = parser.currentName();
            String index = null;
            String type = null;
            String id = null;
            String routing = null;
            String parent = null;
            String timestamp = null;
            Long ttl = null;
            long version = 0L;
            VersionType versionType = VersionType.INTERNAL;

            String currentFieldName = null;
            while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                if (token == XContentParser.Token.FIELD_NAME) {
                    currentFieldName = parser.currentName();
                } else if (token.isValue()) {
                    if ("_index".equals(currentFieldName)) {
                        index = parser.text();
                    } else if ("_type".equals(currentFieldName)) {
                        type = parser.text();
                    } else if ("_id".equals(currentFieldName)) {
                        id = parser.text();
                    } else if ("_routing".equals(currentFieldName) || "routing".equals(currentFieldName)) {
                        routing = parser.text();
                    } else if ("_parent".equals(currentFieldName) || "parent".equals(currentFieldName)) {
                        parent = parser.text();
                    } else if ("_timestamp".equals(currentFieldName) || "timestamp".equals(currentFieldName)) {
                        timestamp = parser.text();
                    } else if ("_ttl".equals(currentFieldName) || "ttl".equals(currentFieldName)) {
                        if (parser.currentToken() == XContentParser.Token.VALUE_STRING) {
                            ttl = TimeValue.parseTimeValue(parser.text(), null).millis();
                        } else {
                            ttl = parser.longValue();
                        }
                    } else if ("_version".equals(currentFieldName) || "version".equals(currentFieldName)) {
                        version = parser.longValue();
                    } else if ("_version_type".equals(currentFieldName) || "_versionType".equals(currentFieldName) || "version_type".equals(currentFieldName) || "versionType".equals(currentFieldName)) {
                        versionType = VersionType.fromString(parser.text());
                    }
                }
            }
            nextMarker = findNextMarker(marker, from, data, length);
            if (nextMarker != -1) {
                if ("index".equals(action)) {
                    request = new IndexRequest(index, type, id).routing(routing).parent(parent).timestamp(timestamp).ttl(ttl).version(version).versionType(versionType)
                            .source(data.slice(from, nextMarker - from), false);
                }
            }
            from = nextMarker + 1;
        } finally {
            parser.close();
        }
       return request;
    }

    private int findNextMarker(byte marker, int from, BytesReference data, int length) {
        for (int i = from; i < length; i++) {
            if (data.get(i) == marker) {
                return i;
            }
        }
        return -1;
    }
}
