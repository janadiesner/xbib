
package org.xbib.web.dispatcher.ber;

import com.google.common.collect.ImmutableMap;
import org.elasticsearch.client.Client;
import org.xbib.web.dispatcher.ApplicationContextListener;
import org.xbib.web.dispatcher.Dispatcher;
import org.xbib.web.dispatcher.DispatcherRequest;
import org.xbib.web.dispatcher.DispatcherSettings;
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
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

@Path("/ber")
public class BERService extends HandlebarsService {

    private final static String template = "html/demo";

    @GET
    public Response get(@Context HttpServletRequest request,
                            @Context UriInfo uriInfo,
                            @QueryParam("index") String index,
                            @QueryParam("indextype") String indextype,
                            @QueryParam("baselibrary") String base,
                            @QueryParam("carrierFilter") String carrierFilter,
                            @QueryParam("id") String id,
                            @QueryParam("year") String yearString,
                            @QueryParam("isil") String isil
    ) throws IOException {
        if (id != null) {
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
                    .put("carrierFilter", carrierFilter)
                    .put("id", id)
                    .put("year", year)
                    .put("isil", isil != null ? isil : "")
                    .build();

            DispatcherSettings dispatcherSettings = DispatcherSettings.Factory.getDispatcherSettings("ber");

            DispatcherRequest dispatcherRequest = new DispatcherRequest()
                    .setIndex(index)
                    .setType(indextype)
                    .setBase(base)
                    .setCarrierFilter(Arrays.asList(carrierFilter))
                    .setSource("zdb")
                    .setIdentifier(id)
                    .setYear(year)
                    .setGroupLimit(10)
                    .setExpandGroups(false)
                    .setGroupFilter(dispatcherSettings.getGroups())
                    .setInstitutionMarker("priority", dispatcherSettings.getPriority())
                    .setInstitutionFilter(Arrays.asList(isil))
                    .setInstitutionCarrierFilter(dispatcherSettings.getServiceRestrictions());

            Client client = ApplicationContextListener.searchSupport.client();
            Dispatcher dispatcher = ApplicationContextListener.dispatcher;

            return Response.ok()
                    .entity(getTemplate(template).apply(makeContext(emptySettings, request,
                    uriInfo, formParams, dispatcher.execute(client, dispatcherRequest))))
                    .build();
        } else {
            // search form only
            return Response.ok().entity(getTemplate(template)
                    .apply(makeContext(emptySettings, request, uriInfo))).build();
        }
    }

    @POST
    public Response post(@Context HttpServletRequest request,
                             @Context UriInfo uriInfo,
                             @FormParam("index") String index,
                             @FormParam("indextype") String indextype,
                             @FormParam("carrierFilter") String carrierFilter,
                             @FormParam("baselibrary") String base,
                             @FormParam("id") String id,
                             @FormParam("year") String yearString,
                             @FormParam("isil") String isil
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
                .put("carrierFilter", carrierFilter)
                .put("id", id)
                .put("year", year)
                .put("isil", isil != null ? isil : "")
                .build();

        DispatcherSettings dispatcherSettings = DispatcherSettings.Factory.getDispatcherSettings("ber");

        DispatcherRequest dispatcherRequest = new DispatcherRequest()
                .setIndex(index)
                .setType(indextype)
                .setBase(base)
                .setCarrierFilter(Arrays.asList(carrierFilter))
                .setSource("zdb")
                .setIdentifier(id)
                .setYear(year)
                .setGroupLimit(10)
                .setExpandGroups(false)
                .setGroupFilter(dispatcherSettings.getGroups())
                .setInstitutionMarker("priority", dispatcherSettings.getPriority())
                .setInstitutionFilter(Arrays.asList(isil))
                .setInstitutionCarrierFilter(dispatcherSettings.getServiceRestrictions());

        Client client = ApplicationContextListener.searchSupport.client();
        Dispatcher dispatcher = ApplicationContextListener.dispatcher;

        return Response.ok()
                .entity(getTemplate(template)
                        .apply(makeContext(emptySettings, request, uriInfo, formParams, dispatcher.execute(client, dispatcherRequest))))
                .build();
    }

    private final static Map<String,Object> emptySettings = newHashMap();

}
