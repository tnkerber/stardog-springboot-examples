package com.polarisalpha.ca.stardog;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import com.complexible.stardog.Stardog;

public class StardogServletContextListener implements ServletContextListener {
    private static Stardog stardog;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        stardog = Stardog.builder().create();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (stardog != null) {
            stardog.shutdown();
        }
    }
}
