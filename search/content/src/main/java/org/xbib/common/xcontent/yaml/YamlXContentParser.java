
package org.xbib.common.xcontent.yaml;

import com.fasterxml.jackson.core.JsonParser;
import org.xbib.common.xcontent.XContentType;
import org.xbib.common.xcontent.json.JsonXContentParser;

public class YamlXContentParser extends JsonXContentParser {

    public YamlXContentParser(JsonParser parser) {
        super(parser);
    }

    @Override
    public XContentType contentType() {
        return XContentType.YAML;
    }
}
