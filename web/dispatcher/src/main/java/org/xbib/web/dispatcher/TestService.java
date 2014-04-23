package org.xbib.web.dispatcher;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.xbib.common.xcontent.XContentHelper;
import org.xbib.io.InputService;
import org.xbib.web.handlebars.HandlebarsService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newLinkedList;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

@Path("/test")
public class TestService extends HandlebarsService {

    private final static Client client = ElasticsearchContextListener.client.client();

    @GET
    public Response test(@Context UriInfo uriInfo, @Context HttpServletRequest request,
                         @QueryParam("q") String queryTerm) throws IOException {
        SearchRequestBuilder searchRequest = client.prepareSearch()
                .setIndices("xbib")
                .setSize(10)
                .setQuery(queryTerm != null ? termQuery("_all", queryTerm) : matchAllQuery());
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        List<Map<String,Object>> list = newLinkedList();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            list.add(hit.getSource());
        }
        return Response.ok(getTemplate("test").apply(makeContext(null, request, uriInfo, list))).build();
    }

    @GET
    @Path("/file")
    public Response testfile(@Context UriInfo uriInfo, @Context HttpServletRequest request) throws IOException {
        List<Map<String,Object>> list = newLinkedList();
        list.add(XContentHelper.convertToMap(InputService.asString(getClass().getResourceAsStream("/test2.json"), "UTF-8")));
        return Response.ok(getTemplate("testfile").apply(makeContext(null, request, uriInfo, list))).build();
    }

}
