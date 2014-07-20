package org.asynchttpclient.generators;

import org.asynchttpclient.Body;
import org.asynchttpclient.BodyGenerator;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * A {@link BodyGenerator} which use an {@link java.io.InputStream} for reading bytes, without having to read the entire
 * stream in memory.
 * <p/>
 * NOTE: The {@link java.io.InputStream} must support the {@link java.io.InputStream#mark} and {@link java.io.InputStream#reset()} operation.
 * If not, mechanisms like authentication, redirect, or resumable download will not works.
 */
public class InputStreamBodyGenerator implements BodyGenerator {

    private final static Logger logger = LoggerFactory.getLogger(InputStreamBodyGenerator.class.getName());
    private final static byte[] END_PADDING = "\r\n".getBytes();
    private final static byte[] ZERO = "0".getBytes();
    private final InputStream inputStream;
    private boolean patchNettyChunkingIssue = false;

    public InputStreamBodyGenerator(InputStream inputStream) {
        this.inputStream = inputStream;

        if (inputStream.markSupported()) {
            inputStream.mark(0);
        }
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public Body createBody() throws IOException {
        return new ISBody();
    }

    protected class ISBody implements Body {
        private boolean eof = false;
        private int endDataCount = 0;
        private byte[] chunk;

        public long getContentLength() {
            return -1;
        }

        public long read(ByteBuffer buffer) throws IOException {

            // To be safe.
            chunk = new byte[buffer.remaining() - 10];


            int read = -1;
            try {
                read = inputStream.read(chunk);
            } catch (IOException ex) {
                logger.warn("Unable to read", ex);
            }

            if (patchNettyChunkingIssue) {
                if (read == -1) {
                    // Since we are chuncked, we must output extra bytes before considering the input stream closed.
                    // chunking requires to end the chunking:
                    // - A Terminating chunk of  "0\r\n".getBytes(),
                    // - Then a separate packet of "\r\n".getBytes()
                    if (!eof) {
                        endDataCount++;
                        if (endDataCount == 2)
                            eof = true;

                        if (endDataCount == 1)
                            buffer.put(ZERO);

                        buffer.put(END_PADDING);


                        return buffer.position();
                    } else {
                        if (inputStream.markSupported()) {
                            inputStream.reset();
                        }
                        eof = false;
                    }
                    return -1;
                }

                /**
                 * Netty 3.2.3 doesn't support chunking encoding properly, so we chunk encoding ourself.
                 */

                buffer.put(Integer.toHexString(read).getBytes());
                // Chunking is separated by "<bytesreads>\r\n"
                buffer.put(END_PADDING);
                buffer.put(chunk, 0, read);
                // Was missing the final chunk \r\n.
                buffer.put(END_PADDING);
            } else {
                if (read > 0) {
                    buffer.put(chunk, 0, read);
                } else {
                    if (inputStream.markSupported()) {
                        inputStream.reset();
                    }
                }
            }
            return read;
        }

        public void close() throws IOException {
            inputStream.close();
        }
    }

    /**
     * HACK: This is required because Netty has issues with chunking.
     *
     * @param patchNettyChunkingIssue
     */
    public void patchNettyChunkingIssue(boolean patchNettyChunkingIssue) {
        this.patchNettyChunkingIssue = patchNettyChunkingIssue;
    }
}
