package mg.matsd.javaframework.core.container;

import mg.matsd.javaframework.core.io.Resource;
import mg.matsd.javaframework.core.managedinstances.factory.ManagedInstanceFactory;
import mg.matsd.javaframework.core.utils.Assert;

public abstract class AbstractXmlResourceContainer extends ManagedInstanceFactory {
    protected String xmlResourceName;

    protected AbstractXmlResourceContainer(String xmlResourceName) {
        setXmlResourceName(xmlResourceName);
    }

    public String getXmlResourceName() {
        return xmlResourceName;
    }

    protected void setXmlResourceName(String xmlResourceName) {
        Assert.notBlank(xmlResourceName, false, "Le fichier de ressource XML ne doit pas être vide ou \"null\"");
        Assert.state(xmlResourceName.endsWith(".xml"),
            () -> new IllegalArgumentException(String.format(
                "La ressource \"%s\" n'est pas un fichier XML", xmlResourceName
            ))
        );

        this.xmlResourceName = xmlResourceName;
    }

    protected void loadManagedInstances() {
        try (Resource resource = buildResource()) {
            Assert.state(resource != null && !resource.isClosed(), "La ressource à utiliser pour charger les \"ManagedInstances\" " +
                "ne peut pas être \"null\" et doit être disponible (non fermée)");

            XMLConfigurationLoader.doLoadManagedInstances(this, resource);
        }
    }

    protected abstract Resource buildResource();
}
