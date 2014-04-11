
package org.xbib.tools.merge.zdb.entities;

import java.util.HashMap;
import java.util.Map;

public class Service extends HashMap<String,Object> implements Comparable<Service> {

    private final Institution institution;

    private final String id;

    private final String group;

    private final Integer priority;

    public Service(Map<String, Object> map, Institution institution) {
        super(map);
        this.institution = institution;
        this.id = (String)get("@id");
        this.group = (String)get("organization");
        this.priority = (Integer)get("priority");
    }

    public String getID() {
        return id;
    }

    public String getGroup() {
        return group;
    }

    public Integer getGroupPriority() {
        return group != null ? institution.getGroupPriority(group) : -1;
    }

    public Integer getPriority() {
        return priority;
    }

    public boolean isActive() {
        boolean b = "interlibrary".equals(get("type"));
        String mode = containsKey("mode") ? get("mode").toString() : "";
        b = b && mode.contains("copy");
        return b;
    }

    @Override
    public int compareTo(Service o) {
        String s1 = ""+getGroupPriority()+getPriority();
        String s2 = ""+o.getGroupPriority()+o.getPriority();
        return s1.compareTo(s2);
    }

}
