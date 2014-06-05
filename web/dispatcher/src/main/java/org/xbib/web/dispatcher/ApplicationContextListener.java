
package org.xbib.web.dispatcher;

import org.xbib.elasticsearch.search.SearchSupport;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ApplicationContextListener implements ServletContextListener {

    private final Logger logger = LoggerFactory.getLogger(ApplicationContextListener.class.getName());

    public static SearchSupport searchSupport;

    public static Dispatcher dispatcher;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        searchSupport = new SearchSupport().newClient();
        logger.info("Elasticsearch client initiated");
        dispatcher = new Dispatcher();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        searchSupport.shutdown();
        logger.info("Elasticsearch client shutdown");
    }

}
