package mg.matsd.javaframework.orm.setup;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.io.ClassPathResource;
import mg.matsd.javaframework.core.io.Resource;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.orm.base.EntityManagerFactory;
import mg.matsd.javaframework.orm.base.SessionFactory;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static mg.matsd.javaframework.core.utils.XMLUtils.*;

public final class Configuration {
    public static final String DEFAULT_CFG_FILENAME  = "database.cfg.xml";
    public static final String PROPERTIES_KEY_PREFIX = "orm.session_factory_";

    private final SessionFactoryOptionsRegistry sessionFactoryOptionsRegistry;

    public Configuration() {
        sessionFactoryOptionsRegistry = new SessionFactoryOptionsRegistry();
    }

    public Configuration configure(String configFileName) {
        Resource resource;
        try {
            resource = new ClassPathResource(configFileName);
        } catch (IllegalArgumentException ignored) {
            throw new IllegalArgumentException("Le nom du fichier de configuration de l'ORM ne peut pas être vide ou \"null\"");
        }

        String resourceName = resource.getName();

        if      (resourceName.endsWith(".properties")) loadConfigsFromProperties(resource);
        else if (resourceName.endsWith(".xml"))        loadConfigsFromXml(resource);
        else throw new IllegalArgumentException(String.format("Fichier au format properties (.properties) ou XML (.xml) attendu mais \"%s\" donné", resourceName));

        resource.close();

        return this;
    }

    public Configuration configure() {
        return configure(DEFAULT_CFG_FILENAME);
    }

    public SessionFactory buildSessionFactory(@Nullable String name) {
        return new EntityManagerFactory(sessionFactoryOptionsRegistry.getSessionFactoryOptions(name).configure());
    }

    public SessionFactory buildSessionFactory() {
        return buildSessionFactory(null);
    }

    private void loadConfigsFromProperties(Resource resource) {
        try {
            Properties properties = new Properties();
            properties.load(resource.getInputStream());

            properties.stringPropertyNames()
                .stream()
                .map(String::strip)
                .filter(propertyName -> propertyName.startsWith(PROPERTIES_KEY_PREFIX))
                .forEachOrdered(propertyName -> {
                    String[] propertyNameParts = propertyName.substring(PROPERTIES_KEY_PREFIX.length()).split("\\.", 2);
                    Assert.state(propertyNameParts.length >= 2,
                        () -> new ConfigurationException(String.format("La déclaration des informations dans un fichier .properties " +
                            "doit être de la forme : %s[session_factory_name].[property_name]", PROPERTIES_KEY_PREFIX))
                    );

                    SessionFactoryOptions sessionFactoryOptions = sessionFactoryOptionsRegistry
                        .getSessionFactoryOptionsOrRegisterIfAbsent(propertyNameParts[0].strip());

                    sessionFactoryOptions.setProperty(propertyNameParts[1].strip(), properties.getProperty(propertyName));
                });
        } catch (IOException e) {
            throw new ConfigurationException(String.format("Erreur lors de la lecture du fichier de configuration : \"%s\"", resource.getName()), e);
        }
    }

    private void loadConfigsFromXml(Resource resource) {
        List<Element> sessionFactoryElements = getChildElementsByTagName(
            buildDocumentElement(Configuration.class.getClassLoader(), resource, "orm-configuration.xsd"),
            "session-factory"
        );

        for (Element sessionFactoryElement : sessionFactoryElements) {
            SessionFactoryOptions sessionFactoryOptions = sessionFactoryOptionsRegistry.registerSessionFactoryOptions(
                getAttributeValue(sessionFactoryElement, "name"));

            List<Element> propertyElements = getChildElementsByTagName(sessionFactoryElement, "property");
            for (Element propertyElement : propertyElements) {
                String propertyValue = getAttributeValue(propertyElement, "value");

                sessionFactoryOptions.setProperty(
                    getAttributeValue(propertyElement, "name"),
                    propertyValue == null ? propertyElement.getTextContent() : propertyValue
                );
            }
        }
    }
}
