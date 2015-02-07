/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.tools.merge.zdb.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Sets.newHashSet;

public class Holding implements Comparable<Holding> {

    protected final Map<String, Object> map;

    protected String identifier;

    protected Set<String> parents = newHashSet();

    protected String isil;

    protected Map<String, Object> info;

    protected Map<String, Object> license;

    protected String mediaType;

    protected String carrierType;

    protected List<Integer> dates;

    protected boolean deleted;

    protected Object servicetype;
    protected Object servicemode;
    protected Object servicedistribution;
    protected Object servicecomment;

    protected Integer priority;

    private List<Manifestation> manifestations = newLinkedList();

    protected String serviceisil;

    private String region;

    private String organization;

    public Holding(Map<String, Object> map) {
        this.map = map;
        build();
    }

    public Map map() {
        return map;
    }

    public String getString(String key) {
        return get(key);
    }

    <T> T get(String key) {
        return this.<T>get(map, key.split("\\."));
    }

    private <T> T get(Map inner, String[] key) {
        if (inner == null) {
            return null;
        }
        Object o = inner.get(key[0]);
        if (o instanceof List) {
            o = ((List) o).get(0); // only first entry
        }
        return (T) (o instanceof Map && key.length > 1 ?
                get((Map) o, Arrays.copyOfRange(key, 1, key.length)) : o);
    }

    protected void build() {
        this.identifier = getString("identifierForTheRecord");
        String parent = getString("identifierForTheParentRecord").toLowerCase(); // DNB-ID, turn 'X' to lower case
        parents.add(parent);
        Object leader = map.get("leader");
        if (!(leader instanceof List)) {
            leader = Arrays.asList(leader);
        }
        for (String s : (List<String>) leader) {
            if ("Deleted".equals(s)) {
                this.deleted = true;
                break;
            }
        }
        Object o = map.get("Location");
        if (!(o instanceof List)) {
            o = Arrays.asList(o);
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>) o;
        for (Map<String, Object> map : list) {
            if (map == null) {
                continue;
            }
            if (map.containsKey("location")) {
                this.serviceisil = (String) map.get("location");
                if (serviceisil != null) {
                    if (serviceisil.startsWith("DE-")) {
                        // find "base" ISIL in DE namespace: cut from last '-' if there is more than one '-'
                        int firstpos = serviceisil.indexOf('-');
                        int lastpos = serviceisil.lastIndexOf('-');
                        this.isil = lastpos > firstpos ? serviceisil.substring(0, lastpos) : serviceisil;
                    } else {
                        this.isil = serviceisil;
                    }
                }
            }
        }
        if (isil == null) {
            // e.g. DNB-ID 036674168 WEU GB-LON63
            this.serviceisil = getString("service.organization"); // Sigel
            this.isil = this.serviceisil; // no conversion to a surrogate ISIL
        }
        // isil may be null, broken holding record, e.g. DNB-ID 114091315 ZDB-ID 2476016x
        if (isil != null) {
            findContentType();
            this.info = buildInfo();
            buildService();
        }
        // serviceisil may be null
        if (serviceisil == null) {
            serviceisil = isil;
        }
        this.dates = buildDateArray();
        this.priority = findPriority();
    }

    public String identifier() {
        return identifier;
    }

    public void addParent(String parent) {
        this.parents.add(parent);
    }

    public Set<String> parents() {
        return parents;
    }

    public String getISIL() {
        return isil;
    }

    public void addManifestation(Manifestation manifestation) {
        this.manifestations.add(manifestation);
    }

    public List<Manifestation> getManifestations() {
        return manifestations;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public String getServiceISIL() {
        return serviceisil != null ? serviceisil : isil;
    }

    public Holding setRegion(String region) {
        this.region = region;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public Holding setOrganization(String organization) {
        this.organization = organization;
        return this;
    }

    public String getOrganization() {
        return organization;
    }

    public Object getServiceType() {
        return servicetype;
    }

    public void setServiceMode(Object servicemode) {
        this.servicemode = servicemode;
    }

    public Object getServiceMode() {
        return servicemode;
    }

    public Object getServiceDistribution() {
        return servicedistribution;
    }

    public Object getServiceComment() {
        return servicecomment;
    }

    public Map<String, Object> getInfo() {
        return info;
    }

    public List<Integer> dates() {
        return dates;
    }

    protected List<Integer> buildDateArray() {
        // no dates by default
        return null;
    }

    public String mediaType() {
        return mediaType;
    }

    public String carrierType() {
        return carrierType;
    }

    public Integer getPriority() {
        return priority;
    }

    protected void findContentType() {
        this.mediaType = "unmediated";
        this.carrierType = "volume";
        if ("EZB".equals(getString("license.origin")) || map.containsKey("ElectronicLocationAndAccess")) {
            this.mediaType = "computer";
            this.carrierType = "online resource";
            return;
        }
        Object o = map.get("textualholdings");
        if (!(o instanceof List)) {
            o = Arrays.asList(o);
        }
        for (String s : (List<String>) o) {
            if (s == null) {
                continue;
            }
            if (s.contains("Microfiche")) {
                this.mediaType = "microform";
                this.carrierType = "other";
                return;
            }
            if (s.contains("CD-ROM") || s.contains("CD-Rom")) {
                this.mediaType = "computer optical disc";
                this.carrierType = "computer disc";
                return;
            }
        }
        String s = getString("SourceOfAcquisition.accessionNumber");
        if (s != null && s.contains("Microfiche")) {
            this.mediaType = "microform";
            this.carrierType = "other";
        }
    }

    private Map<String, Object> buildInfo() {
        Map<String, Object> info = newLinkedHashMap();
        List l = new ArrayList();
        for (String locKey : new String[]{"Location","AdditionalLocation"}) {
            Object o = map.get(locKey);
            if (o != null) {
                if (!(o instanceof List)) {
                    o = Arrays.asList(o);
                }
                for (Map<String,Object> oldlocation : (List<Map<String,Object>>)o) {
                    Map<String,Object> location = newHashMap();
                    // Beschreibung
                    if (oldlocation.containsKey("collection")) {
                        location.put("collection", oldlocation.get("collection"));
                    }
                    // Signatur
                    if (oldlocation.containsKey("shelvinglocation")) {
                        location.put("callnumber", oldlocation.get("shelvinglocation"));
                    }
                    // Notiz, e.g. Digitalisierungsmaster
                    if (oldlocation.containsKey("publicnote")) {
                        location.put("publicnote", oldlocation.get("publicnote"));
                    }
                    // Bestandsverlauf
                    if (oldlocation.containsKey("enumerationAndChronology")) {
                        location.put("enumerationAndChronology", oldlocation.get("enumerationAndChronology"));
                    }
                    if (!location.isEmpty()) {
                        l.add(location);
                    }
                }
            }
        }
        info.put("location", l);

        Object textualholdings = map.get("textualholdings");
        info.put("textualholdings", textualholdings);
        info.put("holdings", map.get("holdings"));
        if (map.containsKey("ElectronicLocationAndAccess")) {
            info.put("links", map.get("ElectronicLocationAndAccess"));
        }
        this.license = (Map<String, Object>) map.get("license");
        if (license != null) {
            license.remove("originSource");
            license.remove("typeSource");
            license.remove("scopeSource");
            license.remove("chargeSource");
            license.remove("accessSource");
            info.put("license", license);
        }
        return info;
    }

    private void buildService() {
        Map<String, Object> service = (Map<String, Object>) map.get("service");
        if (service != null) {
            //setOrganization((String)service.remove("region"));
            service.remove("organization"); // drop Sigel
            service.remove("region"); // drop region marker here, we use bibdat
            service.remove("bik"); // not required
            service.remove("servicetypeSource");  // not required
            service.remove("servicemodeSource"); // not required
            this.servicetype = service.remove("servicetype");
            map.put("type", this.servicetype);
            Object o = service.remove("servicemode");
            // split to array if copy-loan
            this.servicemode = "copy-loan".equals(o) ? Arrays.asList("copy", "loan") : o;
            map.put("mode", servicemode);
            // in ZDB holdings there is no distribution information, but wek keep it here
            this.servicedistribution = service.remove("servicedistribution");
            map.put("distribution", this.servicedistribution);
        }
    }

    protected Integer findPriority() {
        if (carrierType == null) {
            return 9;
        }
        switch (carrierType) {
            case "online resource":
                return (servicedistribution != null && servicedistribution.toString().contains("postal")) ? 3 : 1;
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

    /**
     * Similarity of holdings: they must have same media type, same
     * carrier type, and same date period (if any).
     *
     * @param holdings the holdings to check for similarity against this holding
     * @return collection of holdings which are similar, or an empty collection if no holding is similar
     */
    protected Collection<Holding> getSame(Collection<Holding> holdings) {
        Collection<Holding> same = newArrayList();
        for (Holding holding : holdings) {
            // same ISIL, media, carrier, from/to?
            if (isil.equals(holding.isil)
                    && serviceisil.equals(holding.serviceisil)
                    && mediaType.equals(holding.mediaType)
                    && carrierType.equals(holding.carrierType)) {

                // check if begin date / end date are the same
                // both no dates?
                if (dates == null && holding.dates == null) {
                    // hit, no dates at all
                    same.add(holding);
                } else if (dates != null && !dates.isEmpty()
                        && holding.dates != null && !holding.dates.isEmpty()) {
                    // compare first date and last date
                    Integer d1 = dates.get(0);
                    Integer d2 = dates.get(dates.size() - 1);
                    Integer e1 = holding.dates.get(0);
                    Integer e2 = holding.dates.get(holding.dates.size() - 1);
                    if (d1.equals(e1) && d2.equals(e2)) {
                        same.add(holding);
                    }
                }
            }
        }
        return same;
    }

    public static Comparator<Holding> getRoutingComparator() {
        return (h1, h2) -> h1.getRoutingKey().compareTo(h2.getRoutingKey());
    }

    public String getRoutingKey() {
        return getRegion() + getPriority() + identifier;
    }

    public String toString() {
        return map.toString();
    }

    @Override
    public int compareTo(Holding o) {
        return identifier().compareTo(o.identifier());
    }
}
