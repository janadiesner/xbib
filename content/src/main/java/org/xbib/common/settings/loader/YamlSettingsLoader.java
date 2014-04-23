
package org.xbib.common.settings.loader;

import java.io.IOException;
import java.util.Map;
import org.xbib.common.xcontent.XContentType;

/**
 * Settings loader that loads (parses) the settings in a yaml format by flattening them
 * into a map.
 */
public class YamlSettingsLoader extends XContentSettingsLoader {

    @Override
    public XContentType contentType() {
        return XContentType.YAML;
    }

    @Override
    public Map<String, String> load(String source) throws IOException {
        // replace tabs with whitespace (yaml does not accept tabs, but many users might use it still...)
        return super.load(source.replace("\t", "  "));
    }
}
