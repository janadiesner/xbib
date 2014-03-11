package org.xbib.tools.elasticsearch.merge.zdb.entities;


import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.xbib.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

public class BibdatLookup {

    private Map<String,String> lookup = newHashMap();

    private Map<String, Set<String>> groups = newHashMap();

    public void buildLookup(Client client, String index) throws IOException {
        int size = 1000;
        long millis = 1000L;
        SearchRequestBuilder searchRequest = client.prepareSearch()
                .setIndices(index)
                .setSize(size)
                .setSearchType(SearchType.SCAN)
                .setScroll(TimeValue.timeValueMillis(millis));
        SearchResponse searchResponse = searchRequest.execute().actionGet();
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
                String key = m.containsKey("Identifier") ?
                        (String)((Map<String, Object>)m.get("Identifier")).get("identifierAuthorityISIL") : null;
                String value = m.containsKey("LibraryService") ?
                        (String)((Map<String, Object>)m.get("LibraryService")).get("libraryServiceRegion") : null;
                if (key != null && value != null) {
                    lookup.put(key, value);
                    Set<String> g = groups.get(value);
                    if (g == null) {
                        g = newHashSet();
                    }
                    g.add(key);
                    groups.put(value, g);
                }
            }
        }
        // index groups
        for (String s : groups.keySet()) {
            XContentBuilder builder = jsonBuilder();
            builder.startObject().array("members", groups.get(s)).endObject();
            client.index(Requests.indexRequest().index(index).type("groups").id(s).source(builder.string())).actionGet();
        }
    }

    public Map<String,String> lookup() {
        return lookup;
    }

    public Map<String,Set<String>> groups() {
        return groups;
    }

}
