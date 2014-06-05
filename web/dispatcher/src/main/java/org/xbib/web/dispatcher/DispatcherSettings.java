package org.xbib.web.dispatcher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DispatcherSettings {

    public final static List<String> priority =
            Arrays.asList("DE-6", "DE-38", "DE-61", "DE-82", "DE-361", "DE-386", "DE-465", "DE-1010");

    public final static Map<String,String> serviceMap = new HashMap<String,String>() {{
        put("NRW", "HBZ");
        put("HAM", "VZG");
        put("NIE", "VZG");
        put("SAA", "VZG");
        put("THU", "VZG");
        put("BAW", "BSZ");
        put("SAX", "BSZ");
        put("BAY", "BVB");
        put("HES", "HEBIS");
        put("BER", "ZIB");
        put("WEU", null);
    }};

    public final static Map<String, List<String>> serviceRestrictions = new HashMap<String, List<String>>() {{
        put("DE-38M", Arrays.asList("volume"));
    }};

    public final static List<String> groups =
            Arrays.asList("NRW", "BAY", "BAW", "SAX", "NIE", "HAM",  "SAA", "THU", "HES", "BER");

}
