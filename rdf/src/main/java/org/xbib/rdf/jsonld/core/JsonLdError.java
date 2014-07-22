package org.xbib.rdf.jsonld.core;

import java.util.Map;

public class JsonLdError extends Exception {

    Map<String, Object> details;
    private Error type;

    public JsonLdError(Error type, Object detail) {
        // TODO: pretty toString (e.g. print whole json objects)
        super(detail == null ? "" : detail.toString());
        this.type = type;
    }

    public JsonLdError(Error type) {
        super("");
        this.type = type;
    }


    public JsonLdError setType(Error error) {
        this.type = error;
        return this;
    }

    ;

    public Error getType() {
        return type;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    @Override
    public String getMessage() {
        final String msg = super.getMessage();
        if (msg != null && !"".equals(msg)) {
            return type.toString() + ": " + msg;
        }
        return type.toString();
    }
}
