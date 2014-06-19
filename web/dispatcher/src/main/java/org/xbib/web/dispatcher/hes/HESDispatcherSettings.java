package org.xbib.web.dispatcher.hes;

import org.xbib.web.dispatcher.DispatcherSettings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HESDispatcherSettings implements DispatcherSettings {

    private final static List<String> priority =
            Arrays.asList();

    private final static Map<String, List<String>> serviceRestrictions = new HashMap<String, List<String>>() {{
    }};

    private final static List<String> groups =
            Arrays.asList("HES", "BAY", "NRW", "BAW", "SAX", "NIE", "HAM",  "SAA", "THU", "BER");

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
