package mg.matsd.javaframework.orm.base;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.io.ClassPathResource;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.orm.connection.DatabaseConnector;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.*;

import static mg.matsd.javaframework.core.utils.XMLUtils.*;

public final class Configuration {
    public static final String DEFAULT_CFG_FILENAME  = "database.cfg.xml";
    public static final String PROPERTIES_KEY_PREFIX = "orm.session_factory_";

    private static final Set<String> VALID_PROPERTY_NAMES = new HashSet<>(Arrays.asList(
        "connection.url", "connection.user", "connection.password", "connection.driver_class", "connection.pool_size",
        "show_sql", "format_sql"
    ));

    private Map<Object, Map<String, String>> configs;
    private Map<String, String> currentSessionFactoryProperties;

    public Configuration(String configFileName) {
        loadConfigs(configFileName);
    }

    public Configuration() {
        this(DEFAULT_CFG_FILENAME);
    }

    public Configuration configure(@Nullable Object sessionFactoryId) {
        Map<String, String> sessionFactoryProperties;
        if (sessionFactoryId != null) {
            if (!configs.containsKey(sessionFactoryId))
                throw new IllegalArgumentException(String.format("Aucune configuration de \"session factory\" trouvée pour l'identifiant spécifié : \"%s\"", sessionFactoryId));

               sessionFactoryProperties = configs.get(sessionFactoryId);
        } else sessionFactoryProperties = configs.entrySet().iterator().next().getValue();

        if (currentSessionFactoryProperties == sessionFactoryProperties) return this;
        currentSessionFactoryProperties = sessionFactoryProperties;

        return this;
    }

    public Configuration configure() {
        return configure(null);
    }

    public SessionFactory buildSessionFactory() {
        Assert.state(currentSessionFactoryProperties != null, "Aucune \"session factory\" n'a été configurée. " +
            "Veuillez d'abord appeler la méthode \"configure\" avant de tenter d'en créer une");

        EntityManagerFactory entityManagerFactory = new EntityManagerFactory(
            new DatabaseConnector(
                currentSessionFactoryProperties.get("connection.url"),
                currentSessionFactoryProperties.get("connection.user"),
                currentSessionFactoryProperties.get("connection.password"),
                currentSessionFactoryProperties.get("connection.driver_class"),
                currentSessionFactoryProperties.get("connection.pool_size")
            ), currentSessionFactoryProperties.get("show_sql"), currentSessionFactoryProperties.get("format_sql")
        );

        currentSessionFactoryProperties = null;
        return entityManagerFactory;
    }

    private void loadConfigs(String configFileName) {
        try (ClassPathResource classPathResource = new ClassPathResource(configFileName)) {
            String resourceName = classPathResource.getName();

            if      (resourceName.endsWith(".properties")) loadConfigsFromProperties(classPathResource);
            else if (resourceName.endsWith(".xml"))        loadConfigsFromXml(classPathResource);
            else throw new IllegalArgumentException(String.format("Fichier au format properties (.properties) ou XML (.xml) attendu mais \"%s\" donné", resourceName));
        }
    }

    private void loadConfigsFromProperties(ClassPathResource classPathResource) {
        try {
            Properties properties = new Properties();
            properties.load(classPathResource.getInputStream());

            configs = new HashMap<>();
            properties.stringPropertyNames()
                .stream()
                .map(String::strip)
                .filter(propertyName -> propertyName.startsWith(PROPERTIES_KEY_PREFIX))
                .forEach(propertyName -> {
                    String[] propertyNameParts = propertyName.substring(PROPERTIES_KEY_PREFIX.length()).split("\\.", 2);
                    Assert.state(propertyNameParts.length >= 2,
                        () -> new ConfigurationException(String.format("La déclaration des informations dans un fichier .properties " +
                            "doit être de la forme : %s[session_factory_id].[property_name]", PROPERTIES_KEY_PREFIX))
                    );

                    propertyNameParts[0] = propertyNameParts[0].strip();

                    Map<String, String> sessionFactoryProperties = configs.get(propertyNameParts[0]);
                    if (sessionFactoryProperties == null) {
                        validateSessionFactoryId(propertyNameParts[0]);

                        sessionFactoryProperties = new HashMap<>();
                        configs.put(propertyNameParts[0], sessionFactoryProperties);
                    }

                    propertyNameParts[1] = propertyNameParts[1].strip();
                    Assert.state(VALID_PROPERTY_NAMES.contains(propertyNameParts[1]),
                        () -> new ConfigurationException(String.format("Le nom de propriété \"%s\" n'est pas valide",
                            propertyNameParts[1])
                        ));
                    ensureUniqueProperty(propertyNameParts[1], propertyNameParts[0], sessionFactoryProperties);

                    sessionFactoryProperties.put(propertyNameParts[1], properties.getProperty(propertyName));
                });
        } catch (IOException e) {
            throw new ConfigurationException(String.format("Erreur lors de la lecture du fichier de configuration : \"%s\"", classPathResource.getName()), e);
        }
    }

    private void loadConfigsFromXml(ClassPathResource classPathResource) {
        List<Element> sessionFactoryElements = getChildElementsByTagName(
            buildDocumentElement(Configuration.class.getClassLoader(), classPathResource, "orm-configuration.xsd"),
            "session-factory"
        );

        configs = new HashMap<>();
        int index = 1;
        for (Element sessionFactoryElement : sessionFactoryElements) {
            Object sessionFactoryId = getAttributeValue(sessionFactoryElement, "name");
            sessionFactoryId = sessionFactoryId != null ? ((String) sessionFactoryId).strip() : index++;
            validateSessionFactoryId(sessionFactoryId);

            configs.put(sessionFactoryId, getSessionFactoryProperties(sessionFactoryElement, sessionFactoryId));
        }
    }

    private void validateSessionFactoryId(Object id) {
        if (id instanceof String string && StringUtils.isBlank(string) && configs.entrySet().size() > 1)
            throw new ConfigurationException("L'identifiant d'une \"session factory\" ne peut pas être vide lorsqu'il y en a plusieurs");

        if (configs.containsKey(id))
            throw new ConfigurationException(String.format("Duplication détectée pour l'identifiant de la \"session factory\" : \"%s\"", id));
    }

    private static Map<String, String> getSessionFactoryProperties(Element sessionFactoryElement, Object sessionFactoryId) {
        Map<String, String> sessionFactoryProperties = new HashMap<>();

        List<Element> propertyElements = getChildElementsByTagName(sessionFactoryElement, "property");
        for (Element propertyElement : propertyElements) {
            String propertyName = getAttributeValue(propertyElement, "name");
            ensureUniqueProperty(propertyName, sessionFactoryId, sessionFactoryProperties);

            String propertyValue = getAttributeValue(propertyElement, "value");
            sessionFactoryProperties.put(propertyName, propertyValue == null ? propertyElement.getTextContent() : propertyValue);
        }

        return sessionFactoryProperties;
    }

    private static void ensureUniqueProperty(String property, Object sessionFactoryId, Map<String, String> sessionFactoryProperties) {
        if (!sessionFactoryProperties.containsKey(property)) return;

        String message = String.format("Duplication détectée pour la propriété \"%s\"", property);
        if (!(sessionFactoryId instanceof String string) || !StringUtils.isBlank(string))
            message += String.format(" de la \"session factory\" avec l'identifiant : \"%s\"", sessionFactoryId);

        throw new ConfigurationException(message);
    }
}
