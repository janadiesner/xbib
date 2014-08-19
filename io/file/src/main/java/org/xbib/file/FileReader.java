package org.xbib.file;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileReader {

    public long read(String path) throws IOException {
        long counter = 0;
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(path, "r");
            FileChannel fileChannel = file.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            int bytes = fileChannel.read(buffer);
            while (bytes != -1) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    buffer.get();
                    counter++;
                }
                buffer.clear();
                bytes = fileChannel.read(buffer);
            }
        } finally {
            try {
                if (file != null) {
                    file.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return counter;
    }
}
