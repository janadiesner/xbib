
package org.xbib.web.dispatcher;

import com.google.common.collect.ImmutableMap;
import org.jboss.resteasy.annotations.Form;
import org.xbib.web.handlebars.HandlebarsService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/demo")
public class DemoService extends HandlebarsService {

    private final static Map<String,Object> settings = new HashMap<String,Object>() {{
        put("form_action", "");
        put("libraries", Arrays.asList("DE-6", "DE-38", "DE-61", "DE-361", "DE-386", "DE-465", "DE-1010"));
    }};

    @GET
    public Response demoGet(@Context HttpServletRequest request, @Context UriInfo uriInfo) throws IOException {
        return Response.ok(getTemplate("demo").apply(makeContext(settings, request, uriInfo))).build();
    }

    @POST
    public Response demoPost(@Context HttpServletRequest request,
                             @Context UriInfo uriInfo,
                             @FormParam("baselibrary") String base,
                             @FormParam("id") String id,
                             @FormParam("year") String yearString,
                             @FormParam("compact") String compactString
    ) throws IOException {
        Integer year = yearString != null && !yearString.isEmpty() ? Integer.parseInt(yearString) : 2014;
        Boolean compact = compactString != null && !compactString.isEmpty() ? Boolean.parseBoolean(compactString) : false;

        Map formParams = ImmutableMap.builder()
                .put("baselibrary", base)
                .put("id", id)
                .put("year", year)
                .put("compact", compact).build();

        Dispatcher dispatcher = new Dispatcher()
                .setClient(ElasticsearchContextListener.client.client())
                .setCompact(compact)
                .setBase(base)
                .setIdentifier(id)
                .setYear(year)
                .setGroupFilter(Arrays.asList("NRW", "BAY", "BAW", "SAX", "NIE", "HAM",  "SAA", "HES", "BER"))
                .setInstitutionMarker("pilot", (List<String>) settings.get("libraries"))
                .setTypeFilter(Arrays.asList("interlibrary"))
                .setModeFilter(Arrays.asList("copy", "copy-loan"));
        return Response.ok(getTemplate("dispatcher")
                .apply(makeContext(settings, request, uriInfo, formParams, dispatcher.execute())))
                .build();
    }

}
