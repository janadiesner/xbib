
package org.xbib.web.dispatcher;

import org.xbib.elasticsearch.SearchSupport;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ElasticsearchContextListener implements ServletContextListener {

    private final Logger logger = LoggerFactory.getLogger(ElasticsearchContextListener.class.getName());

    public static SearchSupport client;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        client = new SearchSupport().newClient();
        logger.info("elasticsearch client initiated");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        client.shutdown();
        logger.info("elasticsearch client shutdown");
    }

}
