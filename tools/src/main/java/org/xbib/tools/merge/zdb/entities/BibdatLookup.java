package org.xbib.tools.merge.zdb.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.io.IOException;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class BibdatLookup {

    private final static Logger logger = LogManager.getLogger(BibdatLookup.class);

    private Map<String, String> region = newHashMap();

    private Map<String, String> organization = newHashMap();

    private Map<String, String> other = newHashMap();

    public void buildLookup(Client client, String index) throws IOException {
        int size = 1000;
        long millis = 1000L;
        SearchRequestBuilder searchRequest = client.prepareSearch()
                .setIndices(index)
                .setSize(size)
                .setSearchType(SearchType.SCAN)
                .setScroll(TimeValue.timeValueMillis(millis));
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        logger.info("bibdat index size = {}", searchResponse.getHits().getTotalHits());
        while (searchResponse.getScrollId() != null) {
            searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(millis))
                    .execute().actionGet();
            SearchHits hits = searchResponse.getHits();
            if (hits.getHits().length == 0) {
                break;
            }
            for (SearchHit hit : hits) {
                Map<String, Object> m = hit.getSource();
                String type = m.containsKey("Organization") ?
                        (String) ((Map<String, Object>) m.get("Organization")).get("organizationType") : null;
                if (type == null) {
                    continue;
                }
                String key = m.containsKey("Identifier") ?
                        (String) ((Map<String, Object>) m.get("Identifier")).get("identifierAuthorityISIL") : null;
                if (key == null) {
                    continue;
                }
                String region = m.containsKey("LibraryService") ?
                        (String) ((Map<String, Object>) m.get("LibraryService")).get("libraryServiceRegion") : null;
                if (region == null) {
                    continue;
                }
                String organization = m.containsKey("LibraryService") ?
                        (String) ((Map<String, Object>) m.get("LibraryService")).get("libraryServiceOrganization") : null;
                if (organization == null) {
                    continue;
                }
                // organization state = "Adresse", "Information"
                String state = m.containsKey("Organization") ?
                        (String) ((Map<String, Object>) m.get("Organization")).get("organizationState") : null;
                if (state == null) {
                    continue;
                }
                if ("Adresse".equals(state) ) {
                    switch (type) {
                        case "Abteilungsbibliothek, Institutsbibliothek, Fachbereichsbibliothek (Universität)":
                        case "Wissenschaftliche Spezialbibliothek":
                        case "Öffentliche Bibliothek":
                        case "Mediathek":
                        case "Zentrale Hochschulbibliothek, nicht Universität":
                        case "Zentrale Universitätsbibliothek":
                        case "Abteilungsbibliothek, Fachbereichsbibliothek (Hochschule, nicht Universität)":
                        case "Regionalbibliothek":
                        case "Öffentliche Bibliothek für besondere Benutzergruppen":
                        case "Nationalbibliothek":
                        case "Zentrale Fachbibliothek":
                        case "Verbundsystem/ -katalog":
                            if (!this.region.containsKey(key)) {
                                this.region.put(key, region);
                            } else {
                                logger.warn("entry {} already exists", key);
                            }
                            if (!this.organization.containsKey(key)) {
                                this.organization.put(key, organization);
                            } else {
                                logger.warn("entry {} already exists", key);
                            }
                            break;
                        default:
                            if (!other.containsKey(key)) {
                                other.put(key, region);
                            } else {
                                logger.warn("entry {} already exists in other", key);
                            }
                            break;
                    }
                } else {
                    if (!other.containsKey(key)) {
                        other.put(key, region);
                    } else {
                        logger.warn("entry {} already exists in other", key);
                    }
                }
            }
        }
    }

    public Map<String, String> lookupRegion() {
        return region;
    }

    public Map<String, String> lookupOrganization() {
        return organization;
    }

    public Map<String, String> lookupOther() {
        return other;
    }

}
