package org.xbib.web.dispatcher;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

public class DispatcherRequest {

    private String index = "xbib";

    private String type = "Volume";

    private boolean compact = false;

    private Integer from = 0;

    private Integer size = 10;

    private String source;

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

    private Map<String,Set<String>> instituionCarrierFilter;

    public DispatcherRequest setIndex(String index) {
        if (index != null && !index.isEmpty()) {
            this.index = index;
        }
        return this;
    }

    public String getIndex() {
        return index;
    }

    public DispatcherRequest setType(String type) {
        if (type != null && !type.isEmpty()) {
            this.type = type;
        }
        return this;
    }

    public String getType() {
        return type;
    }

    public DispatcherRequest setCompact(boolean compact) {
        this.compact = compact;
        return this;
    }

    public boolean isCompact() {
        return compact;
    }

    public DispatcherRequest setBase(String base) {
        if (base != null && !base.isEmpty()) {
            this.base = base;
            setInstitutionMarker("base", Arrays.asList(base));
        }
        return this;
    }

    public String getBase() {
        return base;
    }

    public DispatcherRequest setBaseGroup(String baseGroup) {
        this.baseGroup = baseGroup;
        return this;
    }

    public String getBaseGroup() {
        return baseGroup;
    }

    public DispatcherRequest setExpandGroups(boolean expand) {
        this.expandGroups = expand;
        return this;
    }

    public boolean isExpandGroups() {
        return expandGroups;
    }

    public DispatcherRequest setSource(String source) {
        this.source = source;
        return this;
    }

    public String getSource() {
        return source;
    }

    public DispatcherRequest setIdentifier(String identifier) {
        this.identifier = identifier != null ? identifier.toLowerCase().replaceAll("\\-","") : null;
        return this;
    }

    public String getIdentifier() {
        return identifier;
    }

    public DispatcherRequest setYear(Integer year) {
        this.year = year;
        return this;
    }

    public Integer getYear() {
        return year;
    }

    public DispatcherRequest setFrom(Integer from) {
        this.from = from;
        return this;
    }

    public Integer getFrom() {
        return from;
    }

    public DispatcherRequest setSize(Integer size) {
        this.size = size;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public DispatcherRequest setGroupLimit(Integer limit) {
        this.groupLimit = limit;
        return this;
    }

    public Integer getGroupLimit() {
        return groupLimit;
    }

    public DispatcherRequest setGroupFilter(List<String> groupFilter) {
        this.groupFilter = !isEmpty(groupFilter) ? Sets.newLinkedHashSet(groupFilter) : null;
        this.groupOrder = Maps.newHashMap();
        for (int i = 0; i < groupFilter.size(); i++) {
            groupOrder.put(groupFilter.get(i), i);
        }
        return this;
    }

    public Set<String> getGroupFilter() {
        return groupFilter;
    }

    public Map<String, Integer> getGroupOrder() {
        return groupOrder;
    }

    public DispatcherRequest setGroupMap(Map<String,String> groupMap) {
        this.groupMap = groupMap;
        return this;
    }

    public Map<String,String> getGroupMap() {
        return groupMap;
    }

    public DispatcherRequest setExcludeGroupFilter(List<String> groupFilter) {
        this.excludeGroupFilter = !isEmpty(groupFilter) ? Sets.newLinkedHashSet(groupFilter) : null;
        return this;
    }

    public Set<String> getExcludeGroupFilter() {
        return excludeGroupFilter;
    }

    public DispatcherRequest setInstitutionFilter(List<String> institutionFilter) {
        this.institutionFilter = !isEmpty(institutionFilter) ? newHashSet(institutionFilter) : null;
        return this;
    }

    public Set<String> getInstitutionFilter() {
        return institutionFilter;
    }

    public DispatcherRequest setExcludeInstitutionFilter(List<String> institutionFilter) {
        this.excludeInstitutionFilter = !isEmpty(institutionFilter) ? newHashSet(institutionFilter) : null;
        return this;
    }

    public Set<String> getExcludeInstitutionFilter() {
        return excludeInstitutionFilter;
    }

    public DispatcherRequest setCarrierFilter(List<String> carrierFilter) {
        this.carrierFilter = !isEmpty(carrierFilter) ? newHashSet(carrierFilter) : null;
        return this;
    }

    public Set<String> getCarrierFilter() {
        return carrierFilter;
    }

    public DispatcherRequest setExcludeCarrierFilter(List<String> carrierFilter) {
        this.excludeCarrierFilter = !isEmpty(carrierFilter) ? newHashSet(carrierFilter) : null;
        return this;
    }

    public Set<String> getExcludeCarrierFilter() {
        return excludeCarrierFilter;
    }

    public DispatcherRequest setInstitutionCarrierFilter(Map<String,List<String>> carrierFilters) {
        for (Map.Entry<String,List<String>> entry : carrierFilters.entrySet()) {
            setInstitutionCarrierFilter(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public DispatcherRequest setInstitutionCarrierFilter(String isil, List<String> carrierFilter) {
        if (instituionCarrierFilter == null) {
            instituionCarrierFilter = newHashMap();
        }
        if (isil != null && carrierFilter != null) {
            instituionCarrierFilter.put(isil, newHashSet(carrierFilter));
        }
        return this;
    }

    public Map<String,Set<String>> getInstituionCarrierFilter() {
        return instituionCarrierFilter;
    }

    public DispatcherRequest setTypeFilter(List<String> typeFilter) {
        this.typeFilter = !isEmpty(typeFilter) ? newHashSet(typeFilter) : null;
        return this;
    }

    public Set<String> getTypeFilter() {
        return typeFilter;
    }

    public DispatcherRequest setExcludeTypeFilter(List<String> typeFilter) {
        this.excludeTypeFilter = !isEmpty(typeFilter) ? newHashSet(typeFilter) : null;
        return this;
    }

    public Set<String> getExcludeTypeFilter() {
        return excludeTypeFilter;
    }

    public DispatcherRequest setModeFilter(List<String> modeFilter) {
        this.modeFilter = !isEmpty(modeFilter) ? newHashSet(modeFilter) : null;
        return this;
    }

    public Set<String> getModeFilter() {
        return modeFilter;
    }

    public DispatcherRequest setExcludeModeFilter(List<String> modeFilter) {
        this.excludeModeFilter = !isEmpty(modeFilter) ? newHashSet(modeFilter) : null;
        return this;
    }

    public Set<String> getExcludeModeFilter() {
        return excludeModeFilter;
    }

    public DispatcherRequest setDistributionFilter(List<String> distributionFilter) {
        this.distributionFilter = !isEmpty(distributionFilter) ? newHashSet(distributionFilter) : null;
        return this;
    }

    public Set<String> getDistributionFilter() {
        return distributionFilter;
    }

    public DispatcherRequest setExcludeDistributionFilter(List<String> distributionFilter) {
        this.excludeDistributionFilter = !isEmpty(distributionFilter) ? newHashSet(distributionFilter) : null;
        return this;
    }

    public Set<String> getExcludeDistributionFilter() {
        return excludeDistributionFilter;
    }

    public DispatcherRequest setInstitutionMarker(String marker, List<String> institutions) {
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

    public Map<String,String> getInstitutionMarker() {
        return institutionMarker;
    }

    private boolean isEmpty(Collection<String> collection) {
        return collection == null
                || collection.isEmpty()
                || !collection.iterator().hasNext()
                || collection.iterator().next() == null
                || collection.iterator().next().isEmpty();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("compact=[").append(compact).append("]")
                .append(" from=").append(from)
                .append(" size=").append(size)
                .append(" source=").append(source)
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


}
