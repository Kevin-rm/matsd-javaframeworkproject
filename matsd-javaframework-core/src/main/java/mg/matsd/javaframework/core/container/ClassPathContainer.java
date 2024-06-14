package mg.matsd.javaframework.core.container;

import mg.matsd.javaframework.core.io.ClassPathResource;
import mg.matsd.javaframework.core.io.Resource;

public class ClassPathContainer extends AbstractXmlResourceContainer {
    public ClassPathContainer(String xmlResourceName) {
        super(xmlResourceName);

        loadManagedInstances();
    }

    @Override
    protected Resource buildResource() {
        return new ClassPathResource(xmlResourceName);
    }
}
