package org.xbib.web.handlebars;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.template.handlebars.Handlebars;
import org.xbib.template.handlebars.HandlebarsContext;
import org.xbib.template.handlebars.Template;
import org.xbib.template.handlebars.context.FieldValueResolver;
import org.xbib.template.handlebars.context.MapValueResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newLinkedList;


public class HandlebarsService {

    protected final static Logger logger = LogManager.getLogger(HandlebarsService.class.getName());

    protected final static Handlebars handlebars = new Handlebars();

    protected final static Map<String,Template> templates = precompile();

    protected String sanitize(String param) {
        return param != null ? param.trim() : null;
    }

    protected HandlebarsContext makeContext(Map<String,Object> settings,
                                            HttpServletRequest request,
                                            UriInfo uriInfo,
                                            List<Map<String,Object>> list) {
        return HandlebarsContext.newBuilder(makeParams(settings, request, uriInfo, null, list))
                .resolver(MapValueResolver.INSTANCE, FieldValueResolver.INSTANCE).build();
    }

    protected HandlebarsContext makeContext(Map<String,Object> settings,
                                            HttpServletRequest request,
                                            UriInfo uriInfo,
                                            Map<String,Object> map) {
        List<Map<String,Object>> list = newLinkedList();
        list.add(map);
        return HandlebarsContext.newBuilder(makeParams(settings, request, uriInfo, null, list))
                .resolver(MapValueResolver.INSTANCE, FieldValueResolver.INSTANCE).build();
    }

    protected HandlebarsContext makeContext(Map<String,Object> settings,
                                            HttpServletRequest request,
                                            UriInfo uriInfo) {
        return HandlebarsContext.newBuilder(makeParams(settings, request, uriInfo, null, null))
                .resolver(MapValueResolver.INSTANCE, FieldValueResolver.INSTANCE).build();
    }

    protected HandlebarsContext makeContext(Map<String,Object> settings,
                                            HttpServletRequest request,
                                            UriInfo uriInfo,
                                            Map formParams,
                                            List<Map<String,Object>> list) {
        return HandlebarsContext.newBuilder(makeParams(settings, request, uriInfo, formParams, list))
                .resolver(MapValueResolver.INSTANCE, FieldValueResolver.INSTANCE).build();
    }

    protected HandlebarsContext makeContext(Map<String,Object> settings,
                                            HttpServletRequest request,
                                            UriInfo uriInfo,
                                            Map formParams,
                                            Map<String,Object> map) {
        List<Map<String,Object>> list = newLinkedList();
        list.add(map);
        return HandlebarsContext.newBuilder(makeParams(settings, request, uriInfo, formParams, list))
                .resolver(MapValueResolver.INSTANCE, FieldValueResolver.INSTANCE).build();
    }

    protected Map<String,Object> makeParams(Map<String,Object> settings,
                                            HttpServletRequest request,
                                            UriInfo uriInfo,
                                            Map formParams,
                                            List<Map<String,Object>> list) {
        HttpSession session  = request.getSession(true);
        // initialize with settings or not
        Map<String, Object> p = settings != null ? new HashMap<String, Object>(settings) : new HashMap<String,Object>();
        // read from session
        if (session != null) {
            Enumeration<String> en = session.getAttributeNames();
            while (en.hasMoreElements()) {
                String s = en.nextElement();
                p.put(s, session.getAttribute(s));
            }
        }
        // read parameter of form
        if (formParams != null) {
            for (Object key : formParams.keySet()) {
                p.put(key.toString(), formParams.get(key));
            }
        }
        // read attributes from request
        Enumeration<String> en = request.getAttributeNames();
        while (en.hasMoreElements()) {
            String s = en.nextElement();
            p.put(s, request.getAttribute(s));
        }
        // read params from REST URI
        if (uriInfo != null) {
            MultivaluedMap<String,String> map = uriInfo.getPathParameters();
            for (String key : map.keySet()) {
                p.put(key, map.get(key).size() > 1 ? map.get(key) : map.getFirst(key));
            }
            map = uriInfo.getQueryParameters();
            for (String key : map.keySet()) {
                p.put(key, map.get(key).size() > 1 ? map.get(key) : map.getFirst(key));
            }
            // form params?
        }
        if (list != null) {
            p.put("list", list);
        }
        // copy everything back to session
        if (session != null) {
            for (String key : p.keySet()) {
                session.setAttribute(key, p.get(key));
            }
        }
        return p;
    }

    public Template getTemplate(String name) {
        if (!templates.containsKey(name)) {
            throw new IllegalArgumentException("no template " + name + " precompiled");
        } else {
           return templates.get(name);
        }
    }

    private static Map<String,Template> precompile() {
        // TODO traverse directory for templates
        String[] names = new String[] {
                "test",
                "testfile",
                "html/demo",
        };
        Map<String,Template> templateMap = new HashMap<String,Template>();
        try {
            for (String name : names) {
                if (name != null) {
                    templateMap.put(name, handlebars.compile(name));
                }
            }
        } catch (IOException e) {
            logger.error("error while precompiling", e);
        }
        return templateMap;
    }
}
