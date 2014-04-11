
package org.xbib.web.dispatcher;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.xbib.common.xcontent.XContentHelper;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.tools.merge.zdb.entities.Institution;
import org.xbib.tools.merge.zdb.entities.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class Dispatcher {

    private final static Logger logger = LoggerFactory.getLogger(Dispatcher.class.getName());

    private Client client;

    private boolean compact = false;

    private Integer from = 0;

    private Integer size = 10;

    private String identifier;

    private Integer year;

    private Map<String,Integer> groupOrder;

    private Map<String,String> groupMap;

    private String base;

    private String baseGroup;

    private Integer groupLimit;

    private boolean expandGroups;

    private Set<String> groupFilter;
    private Set<String> excludeGroupFilter;

    private Set<String> institutionFilter;
    private Set<String> excludeInstitutionFilter;

    private Set<String> carrierFilter;
    private Set<String> excludeCarrierFilter;

    private Set<String> typeFilter;
    private Set<String> excludeTypeFilter;

    private Set<String> modeFilter;
    private Set<String> excludeModeFilter;

    private Set<String> distributionFilter;
    private Set<String> excludeDistributionFilter;

    private Map<String, String> institutionMarker;

    public Dispatcher setClient(Client client) {
        this.client = client;
        return this;
    }

    public Dispatcher setCompact(boolean compact) {
        this.compact = compact;
        return this;
    }

    public Dispatcher setBase(String base) {
        if (base != null && !base.isEmpty()) {
            this.base = base;
            setInstitutionMarker("base", Arrays.asList(base));
        }
        return this;
    }

    public Dispatcher setBaseGroup(String baseGroup) {
        this.baseGroup = baseGroup;
        return this;
    }

    public Dispatcher setExpandGroups(boolean expand) {
        this.expandGroups = expand;
        return this;
    }

    public Dispatcher setIdentifier(String identifier) {
        this.identifier = identifier != null ? identifier.toLowerCase().replaceAll("\\-","") : null;
        return this;
    }

    public Dispatcher setYear(Integer year) {
        this.year = year;
        return this;
    }

    public Dispatcher setFrom(Integer from) {
        this.from = from;
        return this;
    }

    public Dispatcher setSize(Integer size) {
        this.size = size;
        return this;
    }

    public Dispatcher setGroupLimit(Integer limit) {
        this.groupLimit = limit;
        return this;
    }

    public Dispatcher setGroupFilter(List<String> groupFilter) {
        this.groupFilter = !isEmpty(groupFilter) ? Sets.newLinkedHashSet(groupFilter) : null;
        this.groupOrder = Maps.newHashMap();
        for (int i = 0; i < groupFilter.size(); i++) {
            groupOrder.put(groupFilter.get(i), i);
        }
        return this;
    }

    public Dispatcher setGroupMap(Map<String,String> groupMap) {
        this.groupMap = groupMap;
        return this;
    }

    public Dispatcher setExcludeGroupFilter(List<String> groupFilter) {
        this.excludeGroupFilter = !isEmpty(groupFilter) ? Sets.newLinkedHashSet(groupFilter) : null;
        return this;
    }

    public Dispatcher setInstitutionFilter(List<String> institutionFilter) {
        this.institutionFilter = !isEmpty(institutionFilter) ? newHashSet(institutionFilter) : null;
        return this;
    }

    public Dispatcher setExcludeInstitutionFilter(List<String> institutionFilter) {
        this.excludeInstitutionFilter = !isEmpty(institutionFilter) ? newHashSet(institutionFilter) : null;
        return this;
    }

    public Dispatcher setCarrierFilter(List<String> carrierFilter) {
        this.carrierFilter = !isEmpty(carrierFilter) ? newHashSet(carrierFilter) : null;
        return this;
    }

    public Dispatcher setExcludeCarrierFilter(List<String> carrierFilter) {
        this.excludeCarrierFilter = !isEmpty(carrierFilter) ? newHashSet(carrierFilter) : null;
        return this;
    }

    public Dispatcher setTypeFilter(List<String> typeFilter) {
        this.typeFilter = !isEmpty(typeFilter) ? newHashSet(typeFilter) : null;
        return this;
    }

    public Dispatcher setExcludeTypeFilter(List<String> typeFilter) {
        this.excludeTypeFilter = !isEmpty(typeFilter) ? newHashSet(typeFilter) : null;
        return this;
    }

    public Dispatcher setModeFilter(List<String> modeFilter) {
        this.modeFilter = !isEmpty(modeFilter) ? newHashSet(modeFilter) : null;
        return this;
    }

    public Dispatcher setExcludeModeFilter(List<String> modeFilter) {
        this.excludeModeFilter = !isEmpty(modeFilter) ? newHashSet(modeFilter) : null;
        return this;
    }

    public Dispatcher setDistributionFilter(List<String> distributionFilter) {
        this.distributionFilter = !isEmpty(distributionFilter) ? newHashSet(distributionFilter) : null;
        return this;
    }

    public Dispatcher setExcludeDistributionFilter(List<String> distributionFilter) {
        this.excludeDistributionFilter = !isEmpty(distributionFilter) ? newHashSet(distributionFilter) : null;
        return this;
    }

    public Dispatcher setInstitutionMarker(String marker, List<String> institutions) {
        if (marker == null || marker.isEmpty()) {
            return this;
        }
        if (institutionMarker == null) {
            institutionMarker = Maps.newHashMap();
        }
        if (!isEmpty(institutions)) {
            institutions.stream()
                    .filter(institution -> !institutionMarker.containsKey(institution))
                    .forEach(institution -> {
                        institutionMarker.put(institution, marker);
                    });
        }
        return this;
    }

    public Map<String,Object> execute() throws IOException {
        if (identifier != null && year != null) {
            GetRequestBuilder getRequest = client.prepareGet()
                    .setIndex("xbib2")
                    .setType("Volume")
                    .setId(identifier + "." + year);
            logger.info("executing get request {}", identifier + "." + year);
            GetResponse getResponse = getRequest.execute().actionGet();
            Map<String,Object> result = newHashMap();
            result.put("found", getResponse.isExists() ? 1 : 0);
            if (getResponse.isExists()) {
                Map<String,Object> source = getResponse.getSourceAsMap();
                Map<String, Object> filtered = filter(toInstitutions(source));
                result.putAll(filtered);
                if (!compact) {
                    result.put("@id", source.get("@id"));
                    result.put("date", source.get("date"));
                    result.put("links", source.get("links"));
                }
            } else {
                logger.warn("no document exists");
            }
            return result;
        } else if (identifier != null) {
            SearchRequestBuilder searchRequest = client.prepareSearch()
                    .setIndices("xbib2")
                    .setTypes("Volume")
                    .setQuery(termQuery("@id", identifier));
            if (from != null && from >= 0) {
                searchRequest.setFrom(from);
            }
            if (size != null && size >= 0) {
                searchRequest.setSize(size);
            }
            logger.info("executing search {}", searchRequest);
            SearchResponse searchResponse = searchRequest.execute().actionGet();
            Map<String, Object> result = newHashMap();
            result.put("found", searchResponse.getHits().getTotalHits());
            List<Map<String, Object>> results = newLinkedList();
            for (SearchHit hit : searchResponse.getHits().getHits()) {
                Map<String, Object> m = newHashMap();
                Map<String,Object> source = hit.getSource();
                Map<String,Object> filtered = filter(toInstitutions(source));
                m.putAll(filtered);
                m.put("year", source.get("date"));
                if (!compact) {
                    m.put("@id", source.get("@id"));
                    m.put("links", source.get("links"));
                }
                results.add(m);
            }
            result.put("results", results);
            return result;
        } else {
            return newHashMap();
        }
    }

    public Map<String,Object> execute(String json) throws IOException {
        Map<String,Object> result = newHashMap();
        Map<String,Object> source = XContentHelper.convertToMap(json);
        Map<String, Object> filtered = filter(toInstitutions(source));
        result.putAll(filtered);
        if (!compact) {
            result.put("@id", source.get("@id"));
            result.put("date", source.get("date"));
            result.put("links", source.get("links"));
        }
        return result;
    }

    public List<Institution> toInstitutions(Map<String,Object> source) throws IOException {
        final List<Institution> list = newLinkedList();
        ((List<Map<String, Object>>) source.get("institution")).stream()
                .forEach(new Consumer<Map<String, Object>>() {
                    @Override
                    public void accept(Map<String, Object> map) {
                        list.add(new Institution(map, groupOrder, groupMap));
                    }
                });
        return list;
    }

    public Map<String, Object> filter(List<Institution> institutions) {
        institutions.stream()
                .forEach(new Consumer<Institution>() {
                    @Override
                    public void accept(Institution institution) {
                        List<Service> filteredServices = institution.getActiveServices().stream()
                                        .filter(service -> includeGroup(service))
                                        .filter(service -> excludeGroup(service))
                                        .filter(service -> includeInstitution(service))
                                        .filter(service -> excludeInstitution(service))
                                        .filter(service -> includeType(service))
                                        .filter(service -> excludeType(service))
                                        .filter(service -> includeMode(service))
                                        .filter(service -> excludeMode(service))
                                        .filter(service -> includeDistribution(service))
                                        .filter(service -> excludeDistribution(service))
                                        .sorted()
                                        .collect(toList());
                        institution.putActiveServices(filteredServices);
                        // difference set
                        Set<Service> diff = newHashSet(institution.getActiveServices());
                        diff.removeAll(filteredServices);
                        List<Service> other = institution.getOtherServices();
                        other.addAll(diff);
                        institution.putOther(other);
                    }
                });

        // a bit of random here, we will sort later also.
        Collections.shuffle(institutions);

        if (compact) {
            // compact display, only institution codes
            List<String> head = newLinkedList();
            List<String> tail = newLinkedList();
            List<String> other = newLinkedList();
            boolean hasBase;
            if (isEmpty(baseGroup)) {
                // no division into groups
                List<String> list = institutions.stream()
                        .filter(inst -> !inst.getActiveServices().isEmpty())
                        .sorted()
                        .map(Institution::getISIL)
                        .collect(toList());
                hasBase = newHashSet(list).contains(base);
                // limit by group limit, move others to otherlist
                splitList(list, groupLimit, head, tail);
                // append other institution services
                other.addAll(institutions.stream()
                        .filter(inst -> !inst.getOtherServices().isEmpty())
                        .sorted()
                        .map(Institution::getISIL)
                        .collect(toList()));
            } else {
                // divide into groups when baseGroup is given
                Map<String, List<String>> groups = institutions.stream()
                        .filter(inst -> !inst.getActiveServices().isEmpty())
                        .filter(inst -> !inst.getGroup().equals("X"))
                        .sorted()
                        .collect(groupingBy(Institution::getGroup,
                                mapping(Institution::getISIL, Collectors.toList())));
                String group = groupMap != null ? groupMap.get(baseGroup) : baseGroup;
                List<String> list = groups.containsKey(group) ? groups.remove(group) : newLinkedList();
                hasBase = newHashSet(list).contains(base);
                splitList(list, groupLimit, head, tail);
                List<String> last = newLinkedList(groups.keySet());
                Collections.shuffle(last);
                splitList(last, groupLimit, head, other);
                groups = institutions.stream()
                        .filter(inst -> !inst.getActiveServices().isEmpty())
                        .filter(inst -> !inst.getGroup().equals("X"))
                        .sorted()
                        .collect(groupingBy(Institution::getGroup,
                                mapping(Institution::getISIL, Collectors.toList())));
                other.addAll(groups.keySet());
            }
            Map<String,Object> result = newHashMap();
            result.put("head", head);
            result.put("tail", tail);
            result.put("other", other);
            result.put("hasBase", hasBase);
            return result;
        } else {
            // no compact display
            List<Institution> head = newLinkedList();
            List<Institution> tail = newLinkedList();
            List<Institution> other = newLinkedList();
            boolean hasBase;
            Map<String, Object> result = newHashMap();
            if (isEmpty(baseGroup)) {
                List<Institution> list = institutions.stream()
                        .filter(inst -> !inst.getActiveServices().isEmpty())
                        .map(i -> i.setMarker(getMarker(i)))
                        .sorted()
                        .collect(toList());
                hasBase = head.stream().anyMatch(i -> i.getISIL().equals(base));
                splitList(list, groupLimit, head, tail);
                other.addAll(institutions.stream()
                        .filter(inst -> !inst.getOtherServices().isEmpty())
                        .filter(inst -> !inst.getGroup().equals("X"))
                        .map(i -> i.setMarker(getMarker(i)))
                        .sorted()
                        .collect(toList()));
            } else {
                // divide into groups, build first/last pair
                Map<String, List<Institution>> groups = institutions.stream()
                        .filter(inst -> !inst.getActiveServices().isEmpty())
                        .map(i -> i.setMarker(getMarker(i)))
                        .sorted()
                        .collect(groupingBy(Institution::getGroup));
                // remove unknown institution groups
                groups.remove("X");
                String group = groupMap != null ? groupMap.get(baseGroup) : baseGroup;
                List<Institution> list = groups.containsKey(group) ? groups.remove(group) : newLinkedList();
                hasBase = list.stream().anyMatch(i -> i.getISIL().equals(base));
                splitList(list, groupLimit, head, tail);
                // append other services
                Map<String, List<Institution>> groupsother = institutions.stream()
                        .filter(inst -> !inst.getOtherServices().isEmpty())
                        .filter(inst -> !inst.getGroup().equals("X"))
                        .map(i -> i.setMarker(getMarker(i)))
                        .sorted()
                        .collect(groupingBy(Institution::getGroup));
                List<String> grouplist = Lists.newLinkedList(groups.keySet());
                Collections.shuffle(grouplist);
                list = groupsother.containsKey(group) ? groupsother.remove(group) : newLinkedList();
                other.addAll(list);
                if (expandGroups) {
                    // append all the group as institutions to head
                    for (String l : grouplist) {
                        head.addAll(groups.get(l));
                    }
                    for (String l : groupsother.keySet()) {
                        other.addAll(groupsother.get(l));
                    }
                } else {
                    // only group names instead of whole group institutions
                    List<String> last = newLinkedList();
                    List<String> lastother = newLinkedList();
                    splitList(grouplist, groupLimit, last, lastother);
                    lastother.addAll(groupsother.keySet());
                    result.put("group", last);
                    result.put("groupcount", last.size());
                    result.put("groupother", lastother);
                    result.put("groupothercount", lastother.size());
                }
            }
            result.put("head", head);
            result.put("headcount", head.size());
            result.put("tail", tail);
            result.put("tailcount", tail.size());
            result.put("other", other);
            result.put("othercount", other.size());
            result.put("hasBase", hasBase);
            return result;
        }
    }

    private boolean includeGroup(Service service) {
        return groupFilter == null ||
                (service.containsKey("organization") &&
                 groupFilter.contains(service.get("organization")));
    }

    private boolean excludeGroup(Service service) {
        return excludeGroupFilter == null ||
                (service.containsKey("organization") &&
                !excludeGroupFilter.contains(service.get("organization")));
    }

    private boolean includeInstitution(Service service) {
        return institutionFilter == null ||
                (service.containsKey("isil") && institutionFilter.contains(service.get("isil")));
    }

    private boolean excludeInstitution(Service service) {
        return excludeInstitutionFilter == null ||
                (service.containsKey("isil") && !excludeInstitutionFilter.contains(service.get("isil")));
    }

    private boolean includeType(Service service) {
        return typeFilter == null ||
                (service.containsKey("type") && typeFilter.contains(service.get("type")));
    }

    private boolean excludeType(Service service) {
        return excludeTypeFilter == null ||
                (service.containsKey("type") && !excludeTypeFilter.contains(service.get("type")));
    }

    private boolean includeMode(Service service) {
        return modeFilter == null ||
                (service.containsKey("mode") && modeFilter.contains(service.get("mode")));
    }

    private boolean excludeMode(Service service) {
        return excludeModeFilter == null ||
                (service.containsKey("mode") && !excludeModeFilter.contains(service.get("mode")));
    }

    private boolean includeDistribution(Service service) {
        return distributionFilter == null ||
                (service.containsKey("distribution") && distributionFilter.contains(service.get("distribution")));
    }

    private boolean excludeDistribution(Service service) {
        return excludeDistributionFilter == null ||
                (service.containsKey("distribution") && !excludeDistributionFilter.contains(service.get("distribution")));
    }

    private String getMarker(Institution institution) {
        return institutionMarker != null ?
             institutionMarker.get(institution.getISIL()) : null;
    }

    private boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    private boolean isEmpty(Collection<String> collection) {
        return collection == null
                || collection.isEmpty()
                || !collection.iterator().hasNext()
                || collection.iterator().next() == null
                || collection.iterator().next().isEmpty();
    }

    private void splitList(List list, Integer split, List a, List b) {
        int pos = split != null && split > 0 && split < list.size() ? split : list.size();
        a.addAll(list.subList(0, pos));
        b.addAll(list.subList(pos, list.size()));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("compact=[").append(compact).append("]")
                .append(" from=").append(from)
                .append(" size=").append(size)
                .append(" identifier=").append(identifier)
                .append(" year=").append(year)
                .append(" base=").append(base)
                .append(" group=").append(baseGroup)
                .append(" groupLimit=").append(groupLimit)
                .append(" groupFilter=").append(groupFilter)
                .append(" groupMap=").append(groupMap)
                .append(" organizationOrder=").append(groupOrder)
                .append(" includeInstituion=").append(institutionFilter)
                .append(" excludeInstitution=").append(excludeInstitutionFilter)
                .append(" includeType=").append(typeFilter)
                .append(" excludeType=").append(excludeTypeFilter)
                .append(" includeMode=").append(modeFilter)
                .append(" excludeMode=").append(excludeModeFilter)
                .append(" includeDistribution=").append(distributionFilter)
                .append(" excludeDistribution=").append(excludeDistributionFilter)
                .append(" markers=").append(institutionMarker);
        return sb.toString();
    }

    public final static List<String> pilot =
            Arrays.asList("DE-6","DE-38","DE-61","DE-361","DE-386","DE-465","DE-1010");

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
}
