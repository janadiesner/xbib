package org.asynchttpclient.resumable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link org.asynchttpclient.resumable.ResumableAsyncHandler.ResumableProcessor} which use a properties file
 * to store the download index information.
 */
public class PropertiesBasedResumableProcessor implements ResumableAsyncHandler.ResumableProcessor {

    private final static Logger logger = LogManager.getLogger(PropertiesBasedResumableProcessor.class.getName());

    private final static File TMP = new File(System.getProperty("java.io.tmpdir"), "ahc");

    private final static String storeName = "ResumableAsyncHandler.properties";

    private final ConcurrentHashMap<String, Long> properties = new ConcurrentHashMap<String, Long>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(String url, long transferredBytes) {
        properties.put(url, transferredBytes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(String uri) {
        if (uri != null) {
            properties.remove(uri);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(Map<String, Long> map) {
        logger.debug("Saving current download state {}", properties.toString());
        FileOutputStream os = null;
        try {

            if (!TMP.exists() && !TMP.mkdirs()) {
                throw new IllegalStateException("Unable to create directory: " + TMP.getAbsolutePath());
            }
            File f = new File(TMP, storeName);
            if (!f.exists() && !f.createNewFile()) {
                throw new IllegalStateException("Unable to create temp file: " + f.getAbsolutePath());
            }
            if (!f.canWrite()) {
                throw new IllegalStateException();
            }

            os = new FileOutputStream(f);

            for (Map.Entry<String, Long> e : properties.entrySet()) {
                os.write((append(e)).getBytes("UTF-8"));
            }
            os.flush();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static String append(Map.Entry<String, Long> e) {
        return new StringBuilder(e.getKey()).append("=").append(e.getValue()).append("\n").toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Long> load() {
        Scanner scan = null;
        try {
            scan = new Scanner(new File(TMP, storeName), "UTF-8");
            scan.useDelimiter("[=\n]");

            String key;
            String value;
            while (scan.hasNext()) {
                key = scan.next().trim();
                value = scan.next().trim();
                properties.put(key, Long.valueOf(value));
            }
            logger.debug("Loading previous download state {}", properties.toString());
        } catch (FileNotFoundException ex) {
            logger.debug("Missing {}", storeName);
        } catch (Throwable ex) {
            // Survive any exceptions
            logger.warn(ex.getMessage(), ex);
        } finally {
            if (scan != null)
                scan.close();
        }
        return properties;
    }
}
