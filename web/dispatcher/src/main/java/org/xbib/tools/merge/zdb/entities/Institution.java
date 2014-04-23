package org.xbib.tools.merge.zdb.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newLinkedList;

public class Institution extends HashMap<String,Object> implements Comparable<Institution> {

    private String group;

    private final Map<String,Integer> groupPriorities;

    private List<Service> activeServices;

    private List<Service> otherServices;

    public Institution(Map<String,Object> map, Map<String,Integer> groupPriorities, Map<String,String> groupMap) {
        super(map);
        makeServices(this);
        this.groupPriorities = groupPriorities;
        String s = firstService().getGroup();
        this.group = groupMap != null ? groupMap.get(s) : s;
        if (group == null) {
            group = "X";
        }
        put("group", group);
    }

    private void makeServices(Institution institution) {
        List<Service> services = newLinkedList();
        List<Service> other = newLinkedList();
        ((List<Map<String,Object>>)get("service")).stream()
                .forEach(map -> {
                    Service service = new Service(map, institution);
                    if (service.isActive()) {
                        services.add(service);
                    } else {
                        other.add(service);
                    }
                });
        putActiveServices(services);
        putOther(other);
    }

    public String getGroup() {
        return group;
    }

    public void putActiveServices(List<Service> services) {
        this.activeServices = services;
        put("service", services);
        put("servicecount", services.size());
    }

    public List<Service> getActiveServices() {
        return activeServices;
    }

    public void putOther(List<Service> services) {
        this.otherServices = services;
        put("other", services);
        put("othercount", services.size());
    }

    public List<Service> getOtherServices() {
        return otherServices;
    }

    public String getISIL() {
        return (String)get("isil");
    }

    public Integer getGroupPriority(String group) {
        return groupPriorities != null ?
                groupPriorities.containsKey(group) ? groupPriorities.get(group) : 0 : 0;
    }

    public Institution setMarker(String marker) {
        if (marker != null) {
            put(marker, true);
        }
        return this;
    }


    @Override
    public int compareTo(Institution o) {
        Service s1 = firstService();
        Service s2 = o.firstService();
        return s1.compareTo(s2);
    }

    private Service firstService() {
         return !activeServices.isEmpty() ? activeServices.get(0) :
                !otherServices.isEmpty() ? otherServices.get(0) : null;
    }
}
