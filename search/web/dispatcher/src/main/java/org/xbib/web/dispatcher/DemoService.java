
package org.xbib.web.dispatcher;

import com.google.common.collect.ImmutableMap;
import org.xbib.web.handlebars.HandlebarsService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/demo")
public class DemoService extends HandlebarsService {

    private final static Map<String,Object> settings = new HashMap<String,Object>() {{
        put("form_action", "");
        put("libraries", Arrays.asList("DE-6", "DE-38", "DE-61", "DE-361", "DE-386", "DE-465", "DE-1010"));
        put("groups", Arrays.asList("NRW", "BAY", "BAW", "SAX", "NIE", "HAM",  "SAA", "THU", "HES", "BER"));
    }};

    @GET
    public Response demoGet(@Context HttpServletRequest request,
                            @Context UriInfo uriInfo,
                            @QueryParam("baselibrary") String base,
                            @QueryParam("id") String id,
                            @QueryParam("year") String yearString
    ) throws IOException {
        if (id != null) {
            logger.info("at GET with identifier {}", id);

            Integer year = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR);
            if (yearString != null && !yearString.isEmpty()) {
                try {
                    year = Integer.parseInt(sanitize(yearString));
                } catch (Exception e) {
                    // ignore
                }
            }

            id = sanitize(id);

            Map formParams = ImmutableMap.builder()
                    .put("baselibrary", base)
                    .put("id", id)
                    .put("year", year)
                    .build();

            Dispatcher dispatcher = new Dispatcher()
                    .setClient(ElasticsearchContextListener.client.client())
                    .setBase(base)
                    .setIdentifier(id)
                    .setYear(year)
                    .setGroupLimit(10)
                    .setExpandGroups(false)
                    .setGroupFilter((List<String>) settings.get("groups"))
                    .setInstitutionMarker("pilot", (List<String>) settings.get("libraries"));

            return Response.ok().entity(getTemplate("html/demo").apply(makeContext(settings, request,
                    uriInfo, formParams, dispatcher.execute()))).build();
        } else {
            // search form only
            logger.info("at GET without identifier");
            return Response.ok().entity(getTemplate("html/demo")
                    .apply(makeContext(settings, request, uriInfo))).build();
        }
    }

    @POST
    public Response demoPost(@Context HttpServletRequest request,
                             @Context UriInfo uriInfo,
                             @FormParam("baselibrary") String base,
                             @FormParam("id") String id,
                             @FormParam("year") String yearString
    ) throws IOException {

        Integer year = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR) ;
        if (yearString != null && !yearString.isEmpty()) {
            try {
                year = Integer.parseInt(sanitize(yearString));
            } catch (Exception e) {
                // ignore
            }
        }

        id = sanitize(id);

        Map formParams = ImmutableMap.builder()
                .put("baselibrary", base)
                .put("id", id)
                .put("year", year)
                .build();

        Dispatcher dispatcher = new Dispatcher()
                .setClient(ElasticsearchContextListener.client.client())
                .setBase(base)
                .setIdentifier(id)
                .setYear(year)
                .setGroupLimit(10)
                .setExpandGroups(false)
                .setGroupFilter((List<String>) settings.get("groups"))
                .setInstitutionMarker("pilot", (List<String>) settings.get("libraries"));

        return Response.ok().entity(getTemplate("html/demo")
                .apply(makeContext(settings, request, uriInfo, formParams, dispatcher.execute()))).build();

    }

}
