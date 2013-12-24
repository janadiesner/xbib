
package org.xbib.common.settings.loader;

import com.google.common.io.Closeables;
import org.xbib.common.io.FastByteArrayInputStream;
import org.xbib.common.io.FastStringReader;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Settings loader that loads (parses) the settings in a properties format.
 *
 *
 */
public class PropertiesSettingsLoader implements SettingsLoader {

    @Override
    public Map<String, String> load(String source) throws IOException {
        Properties props = new Properties();
        FastStringReader reader = new FastStringReader(source);
        try {
            props.load(reader);
            Map<String, String> result = newHashMap();
            for (Map.Entry entry : props.entrySet()) {
                result.put((String) entry.getKey(), (String) entry.getValue());
            }
            return result;
        } finally {
            Closeables.close(reader, true);
        }
    }

    @Override
    public Map<String, String> load(byte[] source) throws IOException {
        Properties props = new Properties();
        FastByteArrayInputStream stream = new FastByteArrayInputStream(source);
        try {
            props.load(stream);
            Map<String, String> result = newHashMap();
            for (Map.Entry entry : props.entrySet()) {
                result.put((String) entry.getKey(), (String) entry.getValue());
            }
            return result;
        } finally {
            Closeables.close(stream, true);
        }
    }
}
