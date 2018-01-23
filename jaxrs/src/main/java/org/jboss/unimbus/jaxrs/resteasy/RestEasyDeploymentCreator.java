package org.jboss.unimbus.jaxrs.resteasy;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.undertow.server.handlers.PathHandler;
import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.cdi.ResteasyCdiExtension;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.unimbus.condition.IfClassPresent;
import org.jboss.unimbus.events.LifecycleEvent;
import org.jboss.unimbus.servlet.DeploymentMetaData;
import org.jboss.unimbus.servlet.Deployments;
import org.jboss.unimbus.servlet.ServletMetaData;


/**
 * Created by bob on 1/15/18.
 */
@IfClassPresent("org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher")
@ApplicationScoped
public class RestEasyDeploymentCreator {

    public void createDeployments(@Observes LifecycleEvent.Scan event) {
        for (Application application : this.applications) {
            this.deployments.addDeployment(createDeployment(application));
        }
    }

    public DeploymentMetaData createDeployment(Application application) {
        ApplicationPath appPath = application.getClass().getAnnotation(ApplicationPath.class);
        String path = "/";
        if (appPath != null) path = appPath.value();
        return createDeployment(application, path);
    }

    public DeploymentMetaData createDeployment(Application application, String contextPath) {
        if (contextPath == null) {
            contextPath = "/";
        }
        if (!contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }

        ResteasyDeployment deployment = new ResteasyDeployment();
        deployment.setInjectorFactoryClass(CdiInjectorFactory.class.getName());
        deployment.setActualResourceClasses(extension.getResources());
        deployment.setActualProviderClasses(extension.getProviders());

        DeploymentMetaData meta = createServletDeployment(deployment, application);
        meta.setContextPath(contextPath);
        return meta;
    }

    public DeploymentMetaData createServletDeployment(ResteasyDeployment deployment, Application application) {
        return createServletDeployment(deployment, application, "/");
    }

    public DeploymentMetaData createServletDeployment(ResteasyDeployment deployment, Application application, String mapping) {
        if (mapping == null) {
            mapping = "/";
        }
        if (!mapping.startsWith("/")) {
            mapping = "/" + mapping;
        }
        if (!mapping.endsWith("/")) {
            mapping += "/";
        }

        mapping = mapping + "*";

        String prefix = null;
        if (!mapping.equals("/*")) {
            prefix = mapping.substring(0, mapping.length() - 2);
        }

        ServletMetaData servlet = new ServletMetaData("ResteasyServlet", HttpServlet30Dispatcher.class);
        servlet.setAsyncSupported(true);
        servlet.setLoadOnStartup(1);
        servlet.addUrlPattern(mapping);
        if (prefix != null) {
            servlet.addInitParam("resteasy.servlet.mapping.prefix", prefix);
        }

        String appName = application.getClass().getSimpleName();
        int dollarLoc = appName.indexOf('$');
        if ( dollarLoc >0 ) {
            appName = appName.substring(0, dollarLoc);
        }
        appName = appName.replace('.', '_' );

        DeploymentMetaData meta = new DeploymentMetaData("jaxrs-" + appName);

        meta.addServletContextAttribute(ResteasyDeployment.class.getName(), deployment);
        meta.addServlet(servlet);
        return meta;
    }


    @Inject
    Instance<Application> applications;

    @Inject
    Deployments deployments;

    @Inject
    ResteasyCdiExtension extension;

    private PathHandler root;
}
