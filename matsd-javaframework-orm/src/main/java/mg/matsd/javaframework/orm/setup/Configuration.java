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

public class Configuration {
    public static final Set<String> SUPPORTED_DBMS   = new HashSet<>(Arrays.asList("mysql", "postgres", "oracle"));
    public static final String DEFAULT_CFG_FILENAME  = "database.cfg.xml";
    public static final String PROPERTIES_KEY_PREFIX = "orm.datasource.";

    private Properties properties;
    private Set<String> availableDatasources;

    public Configuration(String configFileName) {
        configure(configFileName);
    }

    public Configuration() {
        this(DEFAULT_CFG_FILENAME);
    }

    public Properties getProperties() {
        return properties;
    }

    public Configuration setProperties(Properties properties) {
        Assert.state(properties != null && !properties.isEmpty(),
            () -> new IllegalArgumentException("L'argument \"properties\" ne peut pas être \"null\" ou vide")
        );

        this.properties = properties;
        return this;
    }

    public Set<String> getAvailableDatasources() {
        return availableDatasources;
    }

    public SessionFactory buildSessionFactory(String datasourceToUse) {
        return new EntityManagerFactory(this, datasourceToUse);
    }

    public SessionFactory buildSessionFactory() {
        return buildSessionFactory(getAvailableDatasources().iterator().next());
    }

    private void configure(String configFileName) {
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
            properties.load(classPathResource.getInputStream());
            availableDatasources = new HashSet<>();

            for (String propertyName : properties.stringPropertyNames()) {
                propertyName = propertyName.strip();
                if (!propertyName.startsWith(PROPERTIES_KEY_PREFIX)) continue;

                String[] propertyNameParts = propertyName.substring(PROPERTIES_KEY_PREFIX.length()).split("\\.");
                Assert.state(propertyNameParts.length == 2,
                    () -> new ConfigurationException(String.format("La déclaration des informations dans un fichier .properties " +
                        "doivent être de la forme : %s.<type-de-base_de_données>.<propriété>", PROPERTIES_KEY_PREFIX))
                );

                propertyNameParts[0] = propertyNameParts[0].strip();
                Assert.state(SUPPORTED_DBMS.contains(propertyNameParts[0]),
                    () -> new UnsupportedDatasourceException(propertyNameParts[0])
                );

                properties.setProperty(propertyName, properties.getProperty(propertyName));
                availableDatasources.add(propertyNameParts[0]);
            }
        } catch (IOException e) {
            throw new ConfigurationException(String.format("Erreur lors de la lecture du fichier de configuration : \"%s\"", classPathResource.getName()), e);
        }
    }

    private void loadPropertiesFromXml(ClassPathResource classPathResource) {
        properties = new Properties();
        availableDatasources = new HashSet<>();

        loadXmlConfigs(classPathResource).forEach((datasourceType, datasourceInfos) -> {
            datasourceInfos.forEach(
                (propertyName, propertyValue) -> properties.setProperty(String.format("%s%s.%s", PROPERTIES_KEY_PREFIX, datasourceType, propertyName), propertyValue)
            );

            availableDatasources.add(datasourceType);
        });
    }

    private static Map<String, Map<String, String>> loadXmlConfigs(ClassPathResource classPathResource) {
        Element documentElement = buildDocumentElement(Configuration.class.getClassLoader(), classPathResource, "orm-configuration.xsd");

        List<Element> datasourceElements = getChildElementsByTagName(documentElement, "datasource");
        if (datasourceElements.isEmpty())
            throw new ConfigurationException("Aucune source de données précisées dans le fichier de configuration");

        Map<String, Map<String, String>> configsFromXml = new HashMap<>();
        for (Element datasourceElement : datasourceElements) {
            String datasourceType = getAttributeValue(datasourceElement, "type").strip().toLowerCase();

            if (!SUPPORTED_DBMS.contains(datasourceType))
                throw new UnsupportedDatasourceException(datasourceType);
            if (configsFromXml.containsKey(datasourceType))
                throw new ConfigurationException(String.format("Duplication détectée pour le type de source de données \"%s\"", datasourceType));

            configsFromXml.put(datasourceType, extractDatasourceInfos(datasourceElement));
        }

        return configsFromXml;
    }

    private static Map<String, String> extractDatasourceInfos(Element datasourceElement) {
        Map<String, String> datasourceInfos = new HashMap<>();

        List<Element> propertyElements = getChildElementsByTagName(datasourceElement, "property");
        for (Element propertyElement : propertyElements) {
            String propertyName = getAttributeValue(propertyElement, "name");
            if (datasourceInfos.containsKey(propertyName))
                throw new ConfigurationException(String.format("Duplication détectée pour le nom de propriété \"%s\"", propertyName));

            String propertyValue = getAttributeValue(propertyElement, "value");
            datasourceInfos.put(propertyName, propertyValue == null ? propertyElement.getTextContent() : propertyValue);
        }

        return datasourceInfos;
    }
}
