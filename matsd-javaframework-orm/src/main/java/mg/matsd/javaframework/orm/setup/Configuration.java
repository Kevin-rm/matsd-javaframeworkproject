package mg.matsd.javaframework.orm.setup;

import mg.matsd.javaframework.core.io.ClassPathResource;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.orm.base.EntityManagerFactory;
import mg.matsd.javaframework.orm.base.SessionFactory;
import mg.matsd.javaframework.orm.exceptions.UnsupportedDatasourceException;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.*;

import static mg.matsd.javaframework.core.utils.XMLUtils.*;

public final class Configuration {
    public static final String DEFAULT_CFG_FILENAME = "database.cfg.xml";

    private static final String PROPERTIES_KEY_PREFIX = "orm.session_factory_";
    private static final Set<String> VALID_PROPERTY_NAMES = new HashSet<>(Arrays.asList(
        "connection.url", "connection.user", "connection.password", "connection.driver_class", "connection.pool_size",
        "show_sql", "format_sql"
    ));

    private Properties properties;

    public Configuration(String configFileName) {
        loadProperties(configFileName);
    }

    public Configuration() {
        this(DEFAULT_CFG_FILENAME);
    }

    public Properties getProperties() {
        return properties;
    }

    public SessionFactory buildSessionFactory(String datasourceToUse) {
        return new EntityManagerFactory(this, datasourceToUse);
    }

    public SessionFactory buildSessionFactory() {
        return buildSessionFactory(getAvailableDatasources().iterator().next());
    }

    private void loadProperties(String configFileName) {
        try (ClassPathResource classPathResource = new ClassPathResource(configFileName)) {
            String resourceName = classPathResource.getName();

            if      (resourceName.endsWith(".properties")) loadProperties       (classPathResource);
            else if (resourceName.endsWith(".xml"))        loadPropertiesFromXml(classPathResource);
            else throw new IllegalArgumentException(String.format("Fichier au format properties (.properties) ou XML (.xml) attendu mais \"%s\" donné", resourceName));
        }
    }

    private void loadProperties(ClassPathResource classPathResource) {
        try {
            properties = new Properties();

            Properties originalProperties = new Properties();
            originalProperties.load(classPathResource.getInputStream());
            for (String originalPropertyName : originalProperties.stringPropertyNames()) {
                originalPropertyName = originalPropertyName.strip();
                if (!originalPropertyName.startsWith(PROPERTIES_KEY_PREFIX)) continue;

                /*
                    orm.session_factory_[id].[property_name] = property_value
                 */
                String[] originalPropertyNameParts = originalPropertyName.substring(PROPERTIES_KEY_PREFIX.length()).split("\\.");
                Assert.state(originalPropertyNameParts.length == 2,
                    () -> new ConfigurationException(String.format("La déclaration des informations dans un fichier .properties " +
                        "doit être de la forme : %s.<type-de-base_de_données>.<propriété>", PROPERTIES_KEY_PREFIX))
                );

                originalPropertyNameParts[0] = originalPropertyNameParts[0].strip().toLowerCase();
                Assert.state(SUPPORTED_DBMS.contains(originalPropertyNameParts[0]),
                    () -> new UnsupportedDatasourceException(originalPropertyNameParts[0])
                );
                originalPropertyNameParts[1] = originalPropertyNameParts[1].strip();
                Assert.state(VALID_PROPERTY_NAMES.contains(originalPropertyNameParts[1]),
                    () ->  new ConfigurationException(String.format("Le nom de propriété \"%s\" n'est pas valide",
                        originalPropertyNameParts[1])
                    ));

                properties.setProperty(String.format("%s.%s", originalPropertyNameParts[0], originalPropertyNameParts[1]), originalProperties.getProperty(originalPropertyName));
            }
        } catch (IOException e) {
            throw new ConfigurationException(String.format("Erreur lors de la lecture du fichier de configuration : \"%s\"", classPathResource.getName()), e);
        }
    }

    private void loadPropertiesFromXml(ClassPathResource classPathResource) {
        properties = new Properties();

        loadConfigsFromXml(classPathResource).forEach((sessionFactoryId, value) ->
            value.forEach((propertyName, propertyValue) -> properties.setProperty(
                String.format("%s%s.%s", PROPERTIES_KEY_PREFIX, sessionFactoryId, propertyName), propertyValue)
            ));
    }

    private static Map<Object, Map<String, String>> loadConfigsFromXml(ClassPathResource classPathResource) {
        List<Element> sessionFactoryElements = getChildElementsByTagName(
            buildDocumentElement(Configuration.class.getClassLoader(), classPathResource, "orm-configuration.xsd"),
            "session-factory"
        );

        Map<Object, Map<String, String>> configsFromXml = new HashMap<>();
        int index = 0;
        for (Element sessionFactoryElement : sessionFactoryElements) {
            Object sessionFactoryId = getAttributeValue(sessionFactoryElement, "name").strip();
            sessionFactoryId = sessionFactoryId != null ? sessionFactoryId : index++;

            if (configsFromXml.containsKey(sessionFactoryId))
                throw new ConfigurationException(String.format("Duplication détectée pour l'identifiant de la session factory : \"%s\"", sessionFactoryId));

            configsFromXml.put(sessionFactoryId, getSessionFactoryProperties(sessionFactoryElement));
        }

        return configsFromXml;
    }

    private static Map<String, String> getSessionFactoryProperties(Element sessionFactoryElement) {
        Map<String, String> sessionFactoryProperties = new HashMap<>();

        List<Element> propertyElements = getChildElementsByTagName(sessionFactoryElement, "property");
        for (Element propertyElement : propertyElements) {
            String propertyName = getAttributeValue(propertyElement, "name");
            if (sessionFactoryProperties.containsKey(propertyName))
                throw new ConfigurationException(String.format("Duplication détectée pour la propriété \"%s\"", propertyName));

            String propertyValue = getAttributeValue(propertyElement, "value");
            sessionFactoryProperties.put(propertyName, propertyValue == null ? propertyElement.getTextContent() : propertyValue);
        }

        return sessionFactoryProperties;
    }
}
