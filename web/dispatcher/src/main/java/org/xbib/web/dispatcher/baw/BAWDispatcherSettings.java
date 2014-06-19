package org.xbib.web.dispatcher.baw;

import org.xbib.web.dispatcher.DispatcherSettings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BAWDispatcherSettings implements DispatcherSettings {

    private final static List<String> priority =
            Arrays.asList();

    private final static Map<String, List<String>> serviceRestrictions = new HashMap<String, List<String>>() {{
    }};

    private final static List<String> groups =
            Arrays.asList("BAW", "BAY", "NRW", "SAX", "NIE", "HAM",  "SAA", "THU", "HES", "BER");

    @Override
    public List<String> getPriority() {
        return priority;
    }

    @Override
    public Map<String, String> getServiceMap() {
        return null;
    }

    @Override
    public Map<String, List<String>> getServiceRestrictions() {
        return serviceRestrictions;
    }

    @Override
    public List<String> getGroups() {
        return groups;
    }
}
