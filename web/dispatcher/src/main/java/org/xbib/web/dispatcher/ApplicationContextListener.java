
package org.xbib.web.dispatcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.elasticsearch.search.SearchSupport;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ApplicationContextListener implements ServletContextListener {

    private final static Logger logger = LogManager.getLogger(ApplicationContextListener.class.getName());

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
