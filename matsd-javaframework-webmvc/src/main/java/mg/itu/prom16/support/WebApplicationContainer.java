package mg.itu.prom16.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mg.itu.prom16.base.Model;
import mg.itu.prom16.base.RedirectData;
import mg.itu.prom16.validation.ModelBindingResult;
import mg.itu.prom16.base.internal.UtilFunctions;
import mg.itu.prom16.base.internal.request.RequestContextHolder;
import mg.itu.prom16.http.SessionImpl;
import mg.matsd.javaframework.core.container.AbstractXmlResourceContainer;
import mg.matsd.javaframework.core.io.Resource;
import mg.matsd.javaframework.core.managedinstances.ManagedInstance;
import mg.matsd.javaframework.core.managedinstances.ManagedInstanceUtils;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.validation.base.ValidatorFactory;

import java.util.ArrayList;
import java.util.List;

public class WebApplicationContainer extends AbstractXmlResourceContainer {
    public static final String WEB_SCOPED_MANAGED_INSTANCES_KEY_PREFIX = "web_scoped_managedinstance";

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
    protected void defineCustomConfiguration() {
        registerManagedInstance(
            new ManagedInstance(SessionImpl.MANAGED_INSTANCE_ID, SessionImpl.class, SESSION, null, null, null),
            new ManagedInstance(Model.MANAGED_INSTANCE_ID, Model.class, REQUEST, null, null, null),
            new ManagedInstance(ThirdPartyConfiguration.MANAGED_INSTANCE_ID, ThirdPartyConfiguration.class, SINGLETON, null, null, null),
            new ManagedInstance(ModelBindingResult.MANAGED_INSTANCE_ID, ModelBindingResult.class, REQUEST, null, null, null),
            new ManagedInstance(RedirectData.MANAGED_INSTANCE_ID, RedirectData.class, REQUEST, null, null, null)
        );

        registerManagedInstance(ThirdPartyConfiguration.JACKSON_OBJECT_MAPPER_ID, ObjectMapper.class, SINGLETON, null, ThirdPartyConfiguration.MANAGED_INSTANCE_ID, "objectMapper");
        registerManagedInstance(ThirdPartyConfiguration.VALIDATOR_FACTORY_ID, ValidatorFactory.class, SINGLETON, null, ThirdPartyConfiguration.MANAGED_INSTANCE_ID, "validatorFactory");
    }

    @Override
    protected Object getManagedInstanceForWebScope(ManagedInstance managedInstance) {
        HttpServletRequest httpServletRequest = RequestContextHolder.getServletRequestAttributes().getRequest();
        String key = WEB_SCOPED_MANAGED_INSTANCES_KEY_PREFIX + managedInstance.getId();

        Object instance;
        if (managedInstance.getScope() == REQUEST) {
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
}
