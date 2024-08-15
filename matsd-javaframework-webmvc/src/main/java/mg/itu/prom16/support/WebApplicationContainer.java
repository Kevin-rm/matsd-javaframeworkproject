package mg.itu.prom16.support;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mg.itu.prom16.base.internal.UtilFunctions;
import mg.itu.prom16.base.internal.request.RequestContextHolder;
import mg.itu.prom16.http.SessionImpl;
import mg.matsd.javaframework.core.container.AbstractXmlResourceContainer;
import mg.matsd.javaframework.core.io.Resource;
import mg.matsd.javaframework.core.managedinstances.ManagedInstance;
import mg.matsd.javaframework.core.managedinstances.ManagedInstanceUtils;
import mg.matsd.javaframework.core.managedinstances.Scope;
import mg.matsd.javaframework.core.utils.Assert;

import java.util.ArrayList;
import java.util.List;

public class WebApplicationContainer extends AbstractXmlResourceContainer {
    public static final String WEB_SCOPED_MANAGED_INSTANCES_PREFIX = "web_scoped_managedinstance";

    private ServletContext servletContext;

    public WebApplicationContainer(ServletContext servletContext, String xmlResourceName) {
        super(xmlResourceName);
        this.setServletContext(servletContext)
            .loadManagedInstances();

        applyConfigurations();
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
    protected Object getManagedInstanceForWebScope(ManagedInstance managedInstance) {
        HttpServletRequest httpServletRequest = RequestContextHolder.getServletRequestAttributes().getRequest();
        String key = WEB_SCOPED_MANAGED_INSTANCES_PREFIX + managedInstance.getId();

        Object instance;
        if (managedInstance.getScope() == Scope.REQUEST) {
            instance = httpServletRequest.getAttribute(key);
            if (instance == null) {
                instance = ManagedInstanceUtils.instantiate(managedInstance, this);
                httpServletRequest.setAttribute(key, instance);
            }
        } else {
            HttpSession httpSession = httpServletRequest.getSession();

            instance = httpSession.getAttribute(key);
            if (instance == null) {
                instance = ManagedInstanceUtils.instantiate(managedInstance, this);
                httpSession.setAttribute(key, instance);
            }
        }

        return instance;
    }

    @Override
    protected Resource buildResource() {
        return new ServletContextResource(servletContext, xmlResourceName);
    }

    private void applyConfigurations() {
        registerManagedInstance(new ManagedInstance(
            "_matsd_session", SessionImpl.class, "session", null, null)
        );
    }
}
