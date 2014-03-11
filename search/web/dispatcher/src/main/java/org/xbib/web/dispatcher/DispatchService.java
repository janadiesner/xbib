package org.xbib.web.dispatcher;

import org.xbib.template.handlebars.Context;
import org.xbib.template.handlebars.Handlebars;
import org.xbib.template.handlebars.Template;
import org.xbib.template.handlebars.context.MapValueResolver;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Path("test")
public class DispatchService {

    private final static Handlebars handlebars = new Handlebars();

    @GET
    public Response get() throws IOException {
        Template template = handlebars.compile("test");

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("message", "Hello World");

        Context context = Context
                .newBuilder(map)
                .resolver(MapValueResolver.INSTANCE)
                .build();
        return Response.ok(template.apply(context)).build();
    }
}
