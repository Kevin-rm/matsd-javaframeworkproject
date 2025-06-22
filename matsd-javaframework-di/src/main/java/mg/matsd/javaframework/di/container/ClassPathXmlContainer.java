package mg.matsd.javaframework.di.container;

import mg.matsd.javaframework.core.io.ClassPathResource;
import mg.matsd.javaframework.core.io.Resource;

public final class ClassPathXmlContainer extends AbstractXmlResourceContainer {

    public ClassPathXmlContainer(String xmlResourceName) {
        super(xmlResourceName);
        loadManagedInstances();
    }

    @Override
    protected Resource buildResource() {
        return new ClassPathResource(xmlResourceName);
    }
}
