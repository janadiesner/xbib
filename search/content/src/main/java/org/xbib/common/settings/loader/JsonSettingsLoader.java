
package org.xbib.common.settings.loader;

import org.xbib.common.xcontent.XContentType;

/**
 * Settings loader that loads (parses) the settings in a json format by flattening them
 * into a map.
 */
public class JsonSettingsLoader extends XContentSettingsLoader {

    @Override
    public XContentType contentType() {
        return XContentType.JSON;
    }
}
