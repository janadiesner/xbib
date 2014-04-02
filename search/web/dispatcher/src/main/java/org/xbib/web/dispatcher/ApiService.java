
package org.xbib.web.dispatcher;

import org.xbib.common.settings.Settings;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static org.xbib.common.settings.ImmutableSettings.settingsBuilder;

@Path("/api/v1")
public class ApiService {

    private final static Logger logger = LoggerFactory.getLogger(ApiService.class.getName());

    @GET
    @Produces("application/json;charset=UTF-8")
    public Response get(@Context UriInfo uriInfo,
                          @QueryParam("from") Integer from,
                          @QueryParam("size") Integer size,
                          @QueryParam("base") String base,
                          @QueryParam("id") String identifier,
                          @QueryParam("year") Integer year,
                          @QueryParam("group") List<String> group,
                          @QueryParam("excludegroup") List<String> excludegroup,
                          @QueryParam("institution") List<String> institution,
                          @QueryParam("excludeinstitution") List<String> excludeinstitution,
                          @QueryParam("type") List<String> type,
                          @QueryParam("excludetype") List<String> excludetype,
                          @QueryParam("mode") List<String> mode,
                          @QueryParam("excludemode") List<String> excludemode,
                          @QueryParam("distribution") List<String> dist,
                          @QueryParam("excludedistribution") List<String> excludedist,
                          @QueryParam("compact") boolean compact
                         ) throws IOException {

        Dispatcher dispatcher = new Dispatcher()
                .setClient(ElasticsearchContextListener.client.client())
                .setCompact(compact)
                .setFrom(from)
                .setSize(size)
                .setBase(base)
                .setIdentifier(identifier)
                .setYear(year)
                .setGroupFilter(group)
                .setExcludeGroupFilter(excludegroup)
                .setInstitutionFilter(institution)
                .setExcludeInstitutionFilter(excludeinstitution)
                .setTypeFilter(type)
                .setExcludeTypeFilter(excludetype)
                .setModeFilter(mode)
                .setExcludeModeFilter(excludemode)
                .setDistributionFilter(dist)
                .setExcludeDistributionFilter(excludedist);


        return Response.ok().entity(dispatcher.execute()).build();
    }

    @POST
    @Produces("application/json;charset=UTF-8")
    @Consumes("application/json")
    public Response post(@Context UriInfo uriInfo,
                          @FormParam("from") Integer from,
                          @FormParam("size") Integer size,
                          @FormParam("base") String base,
                          @FormParam("id") String identifier,
                          @FormParam("year") Integer year,
                          @FormParam("group") List<String> group,
                          @FormParam("excludegroup") List<String> excludegroup,
                          @FormParam("institution") List<String> institution,
                          @FormParam("excludeinstitution") List<String> excludeinstitution,
                          @FormParam("type") List<String> type,
                          @FormParam("excludetype") List<String> excludetype,
                          @FormParam("mode") List<String> mode,
                          @FormParam("excludemode") List<String> excludemode,
                          @FormParam("distribution") List<String> dist,
                          @FormParam("excludedistribution") List<String> excludedist,
                          @FormParam("compact") boolean compact,
                          InputStream in
    ) throws IOException {

        if (in != null) {
            Settings settings = settingsBuilder()
                    .loadFromReader(new InputStreamReader(in, "UTF-8")).build();
            logger.info("got settings from POST body {}", settings.getAsMap());
        }

        Dispatcher dispatcher = new Dispatcher()
                .setClient(ElasticsearchContextListener.client.client())
                .setCompact(compact)
                .setFrom(from)
                .setSize(size)
                .setBase(base)
                .setIdentifier(identifier)
                .setYear(year)
                .setGroupFilter(group)
                .setExcludeGroupFilter(excludegroup)
                .setInstitutionFilter(institution)
                .setExcludeInstitutionFilter(excludeinstitution)
                .setTypeFilter(type)
                .setExcludeTypeFilter(excludetype)
                .setModeFilter(mode)
                .setExcludeModeFilter(excludemode)
                .setDistributionFilter(dist)
                .setExcludeDistributionFilter(excludedist);

        return Response.ok().entity(dispatcher.execute()).build();
    }

    @GET
    @Path("/nrw")
    @Produces("application/json;charset=UTF-8")
    public Response nrwget(@QueryParam("base") String base,
                             @QueryParam("id") String identifier,
                             @QueryParam("year") Integer year,
                             @QueryParam("compact") boolean compact
    ) throws IOException {
        return execute(base, identifier, year, compact);
    }

    @POST
    @Path("/nrw")
    @Produces("application/json;charset=UTF-8")
    public Response nrwpost(@FormParam("base") String base,
            @FormParam("id") String identifier,
            @FormParam("year") Integer year,
            @FormParam("compact") boolean compact
    ) throws IOException {
        return execute(base, identifier, year, compact);
    }

    private Response execute(String base, String identifier, Integer year, boolean compact) throws IOException {

        Dispatcher dispatcher = new Dispatcher()
                .setClient(ElasticsearchContextListener.client.client())
                .setCompact(compact)
                .setBase(base)
                .setIdentifier(identifier)
                .setYear(year)
                .setGroupFilter(Arrays.asList("NRW"))
                .setInstitutionMarker("pilot", Arrays.asList("DE-6","DE-38","DE-61","DE-361","DE-386","DE-465","DE-1010"))
                .setTypeFilter(Arrays.asList("interlibrary"))
                .setModeFilter(Arrays.asList("copy", "copy-loan"));

        return Response.ok().entity(dispatcher.execute()).build();
    }
}
