package org.xbib.tools.merge.zdb.entities;

import org.xbib.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class VolumeHolding extends Holding {

    private final Volume volume;

    public VolumeHolding(Map<String, Object> map, Volume volume) {
        super(map);
        this.volume = volume;
        this.identifier = makeIdentity(volume);
    }

    private String makeIdentity(Volume volume) {
        StringBuilder sb = new StringBuilder();
        sb.append(volume.id()).append('_').append(isil);
        if (map.containsKey("callnumber")) {
            sb.append('_').append(map.get("callnumber"));
        }
        if (map.containsKey("shelfmark")) {
            sb.append('_').append(map.get("shelfmark"));
        }
        return sb.toString();
    }

    protected void build() {
        this.isil = getString("member");
        Object o = get("interlibraryservice");
        if (o != null) {
            if (!(o instanceof List)) {
                o = Arrays.asList(o);
            }
            this.servicemode = o;
            this.servicetype = "interlibrary";
        }
        this.info = buildInfo();
    }

    public VolumeHolding setMediaType(String mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public VolumeHolding setCarrierType(String carrierType) {
        this.carrierType = carrierType;
        this.priority = findPriority();
        return this;
    }

    public VolumeHolding setDate(Integer... date) {
        this.dates = Arrays.asList(date);
        return this;
    }

    protected Map<String, Object> buildInfo() {
        Map<String, Object> m = newHashMap();
        return m;
    }

    public String getStatus() {
        return (String)map.get("status");
    }

    @Override
    protected Integer findPriority() {
        if (carrierType == null) {
            return 9;
        }
        switch (carrierType) {
            case "online resource":
                return (servicedistribution != null
                        && servicedistribution.toString().contains("postal")) ? 3 : 1;
            case "volume":
                return 2;
            case "computer disc":
                return 4;
            case "computer tape cassette":
                return 4;
            case "computer chip cartridge":
                return 4;
            case "microform":
                return 5;
            case "other":
                return 6;
            default:
                throw new IllegalArgumentException("unknown carrier: " + carrierType());
        }
    }

    public String build(XContentBuilder builder, String tag)
            throws IOException {
        String taggedId = tag != null ? tag + "." + volume.externalID : volume.externalID;
        builder.startObject();
        builder.field("@id", taggedId)
                .field("@type", "VolumeHolding")
                .array("@parent", parents)
                .fieldIfNotNull("@tag", tag);
        builder.field("date", dates.get(0))
                .startObject("institution")
                .field("@id", isil)
                .startObject("service")
                .array("@parent", parents)
                .field("@type", "Service")
                .field("mediatype", mediaType)
                .field("carriertype", carrierType)
                .field("organization", getOrganization())
                .field("isil", getISIL())
                .field("serviceisil", getISIL())
                .field("priority", priority)
                .field("type", servicetype);
        // grr
        if (servicemode instanceof List) {
            builder.array("mode", (List) servicemode);
        } else {
            builder.field("mode", servicemode);
        }
        builder.startObject("info")
                .startObject("location")
                        // https://www.hbz-nrw.de/dokumentencenter/produkte/verbunddatenbank/aktuell/plausi/Exemplar-Online-Kurzform.pdf
                .fieldIfNotNull("collection", map.get("shelfmark")) // 088 b sublocation (Standort)
                .fieldIfNotNull("callnumber", map.get("callnumber")) // 088 c (Signatur)
                        //.fieldIfNotNull("collection", map.get("collection")) // 088 d zus. Bestandsangabe (nicht vorhanden)

                .endObject();
        builder.endObject().endObject();
        return taggedId;
    }
}