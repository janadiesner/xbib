package org.xbib.io.posix;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xbib.io.posix.solaris.SolarisHeapFileStat;

import java.io.File;
import java.io.IOException;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;


public class FileStatTest {

    public FileStatTest() {
    }

    private static POSIX posix;

    @BeforeClass
    public static void setUpClass() throws Exception {
        posix = POSIXFactory.getPOSIX(new DummyPOSIXHandler(), true);
    }

    @Test
    public void filestat() throws IOException {
        File f = File.createTempFile("stat", null);
        FileStat st = posix.stat(f.getAbsolutePath());
        f.delete();
        System.err.println(st.ftype());
        assertNotNull("posix.stat failed", st);
    }

    @Test
    public void structStatSize() throws Throwable {
        if (Platform.IS_SOLARIS && Platform.IS_32_BIT) {
            assertEquals("struct size is wrong", 144, new SolarisHeapFileStat().getStructSize());
        }
    }
}