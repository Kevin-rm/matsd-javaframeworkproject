package mg.matsd.javaframework.di.container;

import mg.matsd.javaframework.core.io.ClassPathResource;
import mg.matsd.javaframework.core.io.Resource;

public final class ClassPathContainer extends AbstractXmlResourceContainer {

    public ClassPathContainer(String xmlResourceName) {
        super(xmlResourceName);
    }

    @Override
    protected Resource buildResource() {
        return new ClassPathResource(xmlResourceName);
    }
}
