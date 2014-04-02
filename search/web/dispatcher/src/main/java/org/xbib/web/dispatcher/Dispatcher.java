
package org.xbib.web.dispatcher;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.tools.merge.zdb.entities.Institution;
import org.xbib.tools.merge.zdb.entities.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class Dispatcher {

    private final static Logger logger = LoggerFactory.getLogger(Dispatcher.class.getName());

    private Client client;

    private boolean compact = false;

    private Integer from = 0;

    private Integer size = 10;

    private String identifier;

    private Integer year;

    private Map<String,Integer> organizationOrder;

    private Set<String> groupFilter;
    private Set<String> excludeGroupFilter;

    private Set<String> institutionFilter;
    private Set<String> excludeInstitutionFilter;

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
            setInstitutionMarker("base", Arrays.asList(base));
        }
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

    public Dispatcher setGroupFilter(List<String> groupFilter) {
        this.groupFilter = !isEmpty(groupFilter) ? Sets.newLinkedHashSet(groupFilter) : null;
        this.organizationOrder = Maps.newHashMap();
        for (int i = 0; i < groupFilter.size(); i++) {
            organizationOrder.put(groupFilter.get(i), i);
        }
        return this;
    }

    public Dispatcher setExcludeGroupFilter(List<String> groupFilter) {
        this.excludeGroupFilter = !isEmpty(groupFilter) ? Sets.newLinkedHashSet(groupFilter) : null;
        return this;
    }

    public Dispatcher setInstitutionFilter(List<String> institutionFilter) {
        this.institutionFilter = !isEmpty(institutionFilter) ? Sets.newHashSet(institutionFilter) : null;
        return this;
    }

    public Dispatcher setExcludeInstitutionFilter(List<String> institutionFilter) {
        this.excludeInstitutionFilter = !isEmpty(institutionFilter) ? Sets.newHashSet(institutionFilter) : null;
        return this;
    }

    public Dispatcher setTypeFilter(List<String> typeFilter) {
        this.typeFilter = !isEmpty(typeFilter) ? Sets.newHashSet(typeFilter) : null;
        return this;
    }

    public Dispatcher setExcludeTypeFilter(List<String> typeFilter) {
        this.excludeTypeFilter = !isEmpty(typeFilter) ? Sets.newHashSet(typeFilter) : null;
        return this;
    }

    public Dispatcher setModeFilter(List<String> modeFilter) {
        this.modeFilter = !isEmpty(modeFilter) ? Sets.newHashSet(modeFilter) : null;
        return this;
    }

    public Dispatcher setExcludeModeFilter(List<String> modeFilter) {
        this.excludeModeFilter = !isEmpty(modeFilter) ? Sets.newHashSet(modeFilter) : null;
        return this;
    }

    public Dispatcher setDistributionFilter(List<String> distributionFilter) {
        this.distributionFilter = !isEmpty(distributionFilter) ? Sets.newHashSet(distributionFilter) : null;
        return this;
    }

    public Dispatcher setExcludeDistributionFilter(List<String> distributionFilter) {
        this.excludeDistributionFilter = !isEmpty(distributionFilter) ? Sets.newHashSet(distributionFilter) : null;
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

    private boolean isEmpty(Collection<String> collection) {
        return collection == null
                || collection.isEmpty()
                || !collection.iterator().hasNext()
                || collection.iterator().next() == null
                || collection.iterator().next().isEmpty();
    }

    public Map<String,Object> execute() throws IOException {
        if (identifier != null && year != null) {
            GetRequestBuilder getRequest = client.prepareGet()
                    .setIndex("xbib")
                    .setType("Volume")
                    .setId(identifier + "." + year);
            logger.info("executing get request {}", identifier + "." + year);
            GetResponse getResponse = getRequest.execute().actionGet();
            Map<String,Object> result = newHashMap();
            result.put("found", getResponse.isExists() ? 1 : 0);
            if (getResponse.isExists()) {
                Map<String,Object> source = getResponse.getSourceAsMap();
                Map<String, Object> filtered = filter(getInstitutions(source));
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
                    .setIndices("xbib")
                    .setTypes("Volume")
                    .setFrom(from)
                    .setSize(size)
                    .setQuery(termQuery("@id", identifier));
            logger.info("executing search {}", searchRequest);
            SearchResponse searchResponse = searchRequest.execute().actionGet();
            Map<String, Object> result = newHashMap();
            result.put("found", searchResponse.getHits().getTotalHits());
            List<Map<String, Object>> results = newLinkedList();
            for (SearchHit hit : searchResponse.getHits().getHits()) {
                Map<String, Object> m = newHashMap();
                Map<String,Object> source = hit.getSource();
                Map<String,Object> filtered = filter(getInstitutions(source));
                m.putAll(filtered);
                if (!compact) {
                    m.put("@id", source.get("@id"));
                    m.put("date", source.get("date"));
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

    private List<Institution> getInstitutions(Map<String,Object> source) throws IOException {
        final List<Institution> list = newLinkedList();
        ((List<Map<String, Object>>) source.get("institution")).stream()
                .forEach(new Consumer<Map<String, Object>>() {
                    @Override
                    public void accept(Map<String, Object> map) {
                        list.add(new Institution(map, organizationOrder));
                    }
                });
        return list;
    }

    private Map<String, Object> filter(List<Institution> institutions) {
        institutions.stream()
                .forEach(new Consumer<Institution>() {
                    @Override
                    public void accept(Institution institution) {
                        List<Service> filteredServices = institution.getServices().stream()
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
                                        .collect(Collectors.toList());
                        institution.putServices(filteredServices);
                    }
                });

        // a bit of random, we will sort later by priorities
        Collections.shuffle(institutions);

        if (compact) {
            List<String> list = institutions.stream()
                    .filter(inst -> !inst.getServices().isEmpty())
                    .sorted()
                    .map(Institution::getISIL)
                    .collect(Collectors.toList());
            Map<String,Object> result = newHashMap();
            result.put("isil", list);
            return result;
        } else {
            List<Institution> list = institutions.stream()
                    .filter(inst -> !inst.getServices().isEmpty())
                    .map(i -> i.setMarker(getMarker(i)))
                    .sorted()
                    .collect(Collectors.toList());
            Map<String, Object> result = newHashMap();
            result.put("institution", list);
            result.put("institutioncount", list.size());
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
}
