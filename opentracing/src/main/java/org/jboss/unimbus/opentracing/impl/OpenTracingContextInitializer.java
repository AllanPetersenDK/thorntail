package org.jboss.unimbus.opentracing.impl;

import java.util.EnumSet;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.server.SpanFinishingFilter;

/**
 * @author Pavel Loffay
 */
@Dependent
@WebListener
public class OpenTracingContextInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.err.println( "CONTEXT INITIALIZED: " + servletContextEvent );
        ServletContext servletContext = servletContextEvent.getServletContext();
        FilterRegistration.Dynamic filterRegistration = servletContext
                .addFilter("tracingFilter", new SpanFinishingFilter(tracer));
        filterRegistration.setAsyncSupported(true);
        filterRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "*");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }

    @Inject
    private Tracer tracer;
}
