
package org.xbib.tools.merge.zdb.entities;

import java.util.HashMap;
import java.util.Map;

public class Service extends HashMap<String,Object> implements Comparable<Service> {

    private final Institution institution;

    private final String id;

    private final Integer priority;

    public Service(Map<String, Object> map, Institution institution) {
        super(map);
        this.institution = institution;
        this.id = (String)get("@id");
        this.priority = (Integer)get("priority");
    }

    public String getID() {
        return id;
    }

    public Integer getGroupPriority() {
        return containsKey("organization") ? institution.getGroupPriority((String)get("organization")) : -1;
    }

    public Integer getPriority() {
        return priority;
    }

    public String toString() {
        return institution.getISIL() + "," + getID() + ":" + getGroupPriority() + "," + getPriority();
    }

    @Override
    public int compareTo(Service o) {
        String s1 = ""+getGroupPriority()+getPriority();
        String s2 = ""+o.getGroupPriority()+o.getPriority();
        return s1.compareTo(s2);
    }

}
