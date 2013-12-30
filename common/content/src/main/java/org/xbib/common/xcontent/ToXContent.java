
package org.xbib.common.xcontent;

import java.io.IOException;
import java.util.Map;

/**
 * An interface allowing to transfer an object to "XContent" using an {@link XContentBuilder}.
 *
 *
 */
public interface ToXContent {

    public static interface Params {
        String param(String key);

        String param(String key, String defaultValue);

        boolean paramAsBoolean(String key, boolean defaultValue);

        Boolean paramAsBooleanOptional(String key, Boolean defaultValue);
    }

    public static final Params EMPTY_PARAMS = new Params() {
        
        public String param(String key) {
            return null;
        }

        
        public String param(String key, String defaultValue) {
            return defaultValue;
        }

        
        public boolean paramAsBoolean(String key, boolean defaultValue) {
            return defaultValue;
        }

        
        public Boolean paramAsBooleanOptional(String key, Boolean defaultValue) {
            return defaultValue;
        }
    };

    public static class MapParams implements Params {

        private final Map<String, String> params;

        public MapParams(Map<String, String> params) {
            this.params = params;
        }

        
        public String param(String key) {
            return params.get(key);
        }

        
        public String param(String key, String defaultValue) {
            String value = params.get(key);
            if (value == null) {
                return defaultValue;
            }
            return value;
        }

        
        public boolean paramAsBoolean(String key, boolean defaultValue) {
            return Boolean.parseBoolean(param(key));
        }

        
        public Boolean paramAsBooleanOptional(String key, Boolean defaultValue) {
            String sValue = param(key);
            if (sValue == null) {
                return defaultValue;
            }
            return !(sValue.equals("false") || sValue.equals("0") || sValue.equals("off"));
        }
    }

    XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException;
}