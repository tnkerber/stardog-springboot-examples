package com.polarisalpha.ca.stardog;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.springframework.beans.factory.annotation.Value;
import com.complexible.stardog.Stardog;

/**
 * Start/Stop the Stardog embedded server on application startup/shutdown
 */
public class StardogServletContextListener implements ServletContextListener {
    private static Stardog stardog = null;

    @Value("${stardog.useEmbeddedServer}")
    private Boolean useEmbeddedServer;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (Boolean.TRUE.equals(useEmbeddedServer)) {
            stardog = Stardog.builder().create();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (stardog != null) {
            stardog.shutdown();
        }
    }
}
