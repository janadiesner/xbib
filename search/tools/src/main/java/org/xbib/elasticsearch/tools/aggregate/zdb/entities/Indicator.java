package org.xbib.elasticsearch.tools.aggregate.zdb.entities;

import org.xbib.util.Strings;

import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newLinkedHashMap;

public class Indicator extends License {

    private final static Integer currentYear = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR);

    public Indicator(Map<String, Object> m) {
        super(m);
    }

    @Override
    protected void build() {
        this.parent = getString("xbib:identifier");
        this.isil = getString("xbib:isil");
        this.dates = buildDateArray();
        this.info = buildInfo();
        this.findContentType();
    }

    @Override
    public Map<String, Object> holdingInfo() {
        return info;
    }

    protected void findContentType() {
        this.mediaType = "computer";
        this.carrierType = "online resource";
    }

    protected List<Integer> buildDateArray() {
        List<Integer> dates = newLinkedList();
        String firstDate = getString("xbib:firstDate");
        int first;
        int last;
        if (!Strings.isNullOrEmpty(firstDate)) {
            first = Integer.parseInt(firstDate);
            String lastDate = getString("xbib:lastDate");
            last = Strings.isNullOrEmpty(lastDate) ?
                    currentYear : Integer.parseInt(lastDate);
            if (first > 0 && last > 0) {
                for (int d = first; d <= last; d++) {
                    dates.add(d);
                }
            }
        }
        return dates;
    }

    private final static Pattern movingWallPattern = Pattern.compile("^[+-](\\d+)Y$");

    private Map<String, Object> buildInfo() {
        Map<String, Object> m = newLinkedHashMap();
        Map<String, Object> service = newLinkedHashMap();
        String servicemode = getString("xbib:interlibraryloanCode");
        if (servicemode != null) {
            switch(servicemode) {
                // 4,5 mio
                case "kxn" : // 1.061.340
                case "kxx" : // 1.376.538
                {
                    service.put("servicetype", "interlibraryloan");
                    service.put("servicemode", "copy");
                    break;
                }
                case "kpn" : // 1.684.164
                case "kpx" : // 104.579
                {
                    service.put("servicetype", "interlibraryloan");
                    service.put("servicemode", "copy-non-electronic");
                }
                case "exn" : // 172.778
                case "exx" : // 116.673
                {
                    service.put("servicetype", "interlibraryloan");
                    service.put("servicemode", "copy-electronic");
                    break;
                }
            }
        }
        service.put("servicecomment", getString("xbib:comment"));
        m.put("service", service);
        Map<String, Object> holdings = new LinkedHashMap();
        holdings.put("firstvolume", getString("xbib:firstVolume"));
        holdings.put("firstissue", getString("xbib:firstIssue"));
        holdings.put("firstdate", getString("xbib:firstDate"));
        holdings.put("lastvolume", getString("xbib:lastVolume"));
        holdings.put("lastissue", getString("xbib:lastIssue"));
        holdings.put("lastdate", getString("xbib:lastDate"));
        m.put("holdings", holdings);
        return m;
    }

}
