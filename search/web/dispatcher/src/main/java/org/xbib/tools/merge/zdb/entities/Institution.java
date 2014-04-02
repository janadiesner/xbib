package org.xbib.tools.merge.zdb.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newLinkedList;

public class Institution extends HashMap<String,Object> implements Comparable<Institution> {

    private final Map<String,Integer> groupPriorities;

    private List<Service> services;

    public Institution(Map<String,Object> map, Map<String,Integer> groupPriorities) {
        super(map);
        this.groupPriorities = groupPriorities;
        putServices(makeServices(this));
    }

    private List<Service> makeServices(Institution institution) {
        List<Service> list = newLinkedList();
        ((List<Map<String,Object>>)get("service")).stream()
                .forEach(map -> {
                    list.add(new Service(map, institution));
                });
        return list;
    }

    public void putServices(List<Service> services) {
        this.services = services;
        put("service", services);
        put("servicecount", services.size());
    }

    public List<Service> getServices() {
        return services;
    }

    public String getISIL() {
        return (String)get("isil");
    }

    public Integer getGroupPriority(String group) {
        return groupPriorities.containsKey(group) ? groupPriorities.get(group) : 0;
    }

    public Institution setMarker(String marker) {
        if (marker != null) {
            put(marker, true);
        }
        return this;
    }

    @Override
    public int compareTo(Institution o) {
        Service s1 = getServices().get(0);
        Service s2 = o.getServices().get(0);
        return s1.compareTo(s2);
    }
}
