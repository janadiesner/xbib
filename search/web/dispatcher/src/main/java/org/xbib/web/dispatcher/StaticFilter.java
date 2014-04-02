
package org.xbib.web.dispatcher;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter(filterName="StaticFilter",urlPatterns={"/*"}, servletNames={"*"})
public class StaticFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * If an URI path contains ".../static/...", move "/static/" to the beginning of the URI path.
     * So all requests to static resources are redirected to src/main/webapp/static folder.
     *
     * @param request the request
     * @param response the response
     * @param chain the chain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getRequestURI().substring(req.getContextPath().length());
        int pos = path.indexOf("/static/");
        if (pos > 0) {
            request.getRequestDispatcher(path.substring(pos)).forward(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }
}

