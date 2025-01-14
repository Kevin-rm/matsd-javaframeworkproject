package mg.matsd.javaframework.core.container;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.io.Resource;
import mg.matsd.javaframework.core.managedinstances.factory.ManagedInstanceFactory;
import mg.matsd.javaframework.core.utils.Assert;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractXmlResourceContainer extends ManagedInstanceFactory {
    protected String xmlResourceName;
    private final List<String> schemas = Arrays.asList("container.xsd", "managedinstances.xsd");

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

    String[] getSchemas() {
        return schemas.toArray(new String[0]);
    }

    protected void registerSchemas(@Nullable String... schemas) {
        if (schemas == null) return;

        for (String schema : schemas) {
            Assert.notBlank(schema, false, "Chaque schéma ne peut pas être vide ou \"null\"");
            Assert.state(schema.endsWith(".xsd"), () -> new IllegalArgumentException(
                String.format("Le schéma donné \"%s\" n'est pas un fichier XSD", schema))
            );
        }
        this.schemas.addAll(List.of(schemas));
    }

    protected void loadManagedInstances() {
        try (Resource resource = buildResource()) {
            Assert.state(resource != null && !resource.isClosed(), "La ressource à utiliser pour charger les \"ManagedInstances\" " +
                "ne peut pas être \"null\" et doit être disponible (non fermée)");

            XMLConfigurationLoader.doLoadManagedInstances(this, resource);
            eagerInitSingletonManagedInstances();
        }
    }

    protected void additionalXmlConfigLoadingLogic(Element documentElement) { }

    protected abstract Resource buildResource();
}
