package mg.matsd.javaframework.core.container;

import mg.matsd.javaframework.core.io.ClassPathResource;
import mg.matsd.javaframework.core.io.Resource;
import mg.matsd.javaframework.core.managedinstances.ManagedInstance;

public class ClassPathContainer extends AbstractXmlResourceContainer {
    public ClassPathContainer(String xmlResourceName) {
        super(xmlResourceName);

        loadManagedInstances();
    }

    @Override
    protected Object getManagedInstanceForWebScope(ManagedInstance managedInstance) {
        throw new UnsupportedOperationException("La méthode \"getManagedInstanceForWebScope\" n'est disponible que dans un contexte web");
    }

    @Override
    protected void customConfiguration() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Resource buildResource() {
        return new ClassPathResource(xmlResourceName);
    }
}
