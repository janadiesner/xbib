package org.xbib.jaxrs;

import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ServerResponse;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.util.Base64;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import java.io.IOException;
import java.util.List;

public class PasswordSecurityFilter implements ContainerRequestFilter {

    private final static Logger logger = LoggerFactory.getLogger(PasswordSecurityFilter.class.getName());

    private static final ServerResponse ACCESS_DENIED = new ServerResponse("Access denied for this resource", 401, new Headers<Object>());

    private static final ServerResponse ACCESS_FORBIDDEN = new ServerResponse("Nobody can access this resource", 403, new Headers<Object>());

    private static final ServerResponse SERVER_ERROR = new ServerResponse("INTERNAL SERVER ERROR", 500, new Headers<Object>());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        logger.debug("filter container request = {}", requestContext);
        Request request = requestContext.getRequest();
        final MultivaluedMap<String, String> headers = requestContext.getHeaders();
        List<String> authorization = headers.get("Authorization");
        logger.debug("filter auth {}", authorization);
        if (authorization == null) {
            requestContext.abortWith(ACCESS_DENIED);
            return;
        }
        if (!authorization.get(0).startsWith("Basic ")) {
            return;
        }
        String auth = Base64.decodeString(authorization.get(0).substring("Basic ".length()));
        logger.debug("filter auth decoded {}", auth);
        String[] values = auth.split(":");
        if (values.length < 2) {
            return;
        }
        PasswordSecurityContext sc = new PasswordSecurityContext(requestContext.getSecurityContext());
        sc.setUser(values[0]);
        sc.setPassword(values[1]);
        requestContext.setSecurityContext(sc);
        logger.debug("request = {} security context = {}", request, sc);
    }
}
