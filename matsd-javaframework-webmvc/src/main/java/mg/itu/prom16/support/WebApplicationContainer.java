package mg.itu.prom16.support;

import jakarta.servlet.ServletContext;
import mg.itu.prom16.base.internal.UtilFunctions;
import mg.matsd.javaframework.core.container.AbstractContainer;
import mg.matsd.javaframework.core.io.Resource;
import mg.matsd.javaframework.core.managedinstances.ManagedInstance;
import mg.matsd.javaframework.core.utils.Assert;

import java.util.ArrayList;
import java.util.List;

public class WebApplicationContainer extends AbstractContainer {
    private ServletContext servletContext;

    public WebApplicationContainer(ServletContext servletContext, String xmlResourceName) {
        super(xmlResourceName);
        this.setServletContext(servletContext)
            .loadManagedInstances();
    }

    private WebApplicationContainer setServletContext(ServletContext servletContext) {
        Assert.notNull(servletContext, "L'argument servletContext ne peut pas être \"null\"");

        this.servletContext = servletContext;
        return this;
    }

    public List<Class<?>> retrieveControllerClasses() {
        List<Class<?>> controllerClasses = new ArrayList<>();

        for (ManagedInstance managedInstance : managedInstanceDefinitionRegistry.getManagedInstances()) {
            Class<?> managedInstanceClass = managedInstance.getClazz();
            if (!UtilFunctions.isController(managedInstanceClass)) continue;

            controllerClasses.add(managedInstanceClass);
        }

        return controllerClasses;
    }

    @Override
    protected Resource buildResource() {
        return new ServletContextResource(servletContext, xmlResourceName);
    }
}
