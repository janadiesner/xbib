package org.xbib.elements.event;

import org.xbib.marc.event.Cause;
import org.xbib.marc.event.Change;

public enum MapEvent implements Cause, Change {

    MAP,
    DROP
    ;

    private String spec;

    MapEvent() {
    }

    public MapEvent setSpec(String spec) {
        this.spec = spec;
        return this;
    }

    public String getSpec() {
        return spec;
    }

    public MapEvent setCause(String cause) {
        return this;
    }

    public String getCause() {
        return null;
    }

    public MapEvent setChange(Object prev, Object next) {
        return this;
    }

    public Object getPrev() {
        return null;
    }

    public Object getNext() {
        return null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[Event:").append(this.name());
        if (getSpec() != null) {
            sb.append(':').append(getSpec());
        }
        if (getCause() != null) {
            sb.append(":cause=").append(getCause());
        }
        if (getPrev() != null) {
            sb.append(":prev=").append(getPrev());
        }
        if (getNext() != null) {
            sb.append(":next=").append(getNext());
        }
        sb.append(']');
        return sb.toString();
    }

    public String toJSON() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"event\":\"").append(this.name()).append("\"");
        if (getSpec() != null) {
            sb.append(",\"field\":\"").append(getSpec()).append("\"");
        }
        if (getCause() != null) {
            sb.append(",\"cause\":\"").append(getCause()).append("\"");
        }
        if (getPrev() != null) {
            sb.append(",\"prev\":\"").append(getPrev()).append("\"");
        }
        if (getNext() != null) {
            sb.append(",\"next\":\"").append(getNext()).append("\"");
        }
        sb.append('}');
        return sb.toString();
    }

    public String toTSV() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name()).append('\t')
                .append(getSpec() != null ? getSpec() : "").append('\t')
                .append(getCause() != null ? getCause() : "").append('\t')
                .append(getPrev() != null ? getPrev() : "").append('\t')
                .append(getNext() != null ? getNext() : "");
        return sb.toString();
    }
}
