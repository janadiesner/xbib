package org.xbib.web.dispatcher.ham;

import org.xbib.web.dispatcher.DispatcherSettings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HAMDispatcherSettings implements DispatcherSettings {

    private final static List<String> priority =
            Arrays.asList();

    private final static Map<String, List<String>> serviceRestrictions = new HashMap<String, List<String>>() {{
    }};

    private final static List<String> groups =
            Arrays.asList("HAM", "SAX", "NIE", "SAA", "THU", "BER", "BAY", "NRW", "BAW", "HES");

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
