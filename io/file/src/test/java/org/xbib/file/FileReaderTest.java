package org.xbib.file;

import org.testng.annotations.Test;

import java.io.IOException;

public class FileReaderTest {

    @Test
    public void test() throws IOException {
        FileReader reader = new FileReader();
        long l = reader.read("/Users/joerg/import/hbz/vk/20140816/clob-20140815-20140816.tar.gz");
        System.err.println("l=" + l);
    }
}
