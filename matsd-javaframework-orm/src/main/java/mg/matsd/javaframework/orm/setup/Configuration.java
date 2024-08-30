package mg.matsd.javaframework.orm.setup;

import mg.matsd.javaframework.core.exceptions.XmlParseException;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.orm.base.EntityManagerFactory;
import mg.matsd.javaframework.orm.base.SessionFactory;
import mg.matsd.javaframework.orm.exceptions.UnsupportedDataSourceException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Configuration {
    public  static final Set<String> SUPPORTED_DBMS   = new HashSet<>(Arrays.asList("mysql", "postgres", "oracle"));
    public  static final String DEFAULT_CFG_FILENAME  = "database.cfg.xml";
    public  static final String PROPERTIES_KEY_PREFIX = "matsd.orm.datasource.";
    private static final Set<String> VALID_PROPERTY_NAMES = new HashSet<>(
        Arrays.asList("host", "port", "database-name", "username", "password", "pool-size")
    );
    private static final Set<String> VALID_PROPERTY_ATTRIBUTES = new HashSet<>(
        Arrays.asList("name", "value")
    );

    private Properties properties;
    private Set<String> availableDataSources;

    public Configuration() {
        configure(DEFAULT_CFG_FILENAME);
    }

    public Configuration(String configFileName) {
        configure(configFileName);
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

    public Set<String> getAvailableDataSources() {
        return availableDataSources;
    }

    public SessionFactory buildSessionFactory(String datasourceToUse) {
        return new EntityManagerFactory(this, datasourceToUse);
    }

    public SessionFactory buildSessionFactory() {
        return buildSessionFactory(getAvailableDataSources().iterator().next());
    }

    private void configure(String configFileName) {
        Assert.notBlank(configFileName, false, "Le nom du fichier de configuration ne peut pas être vide ou \"null\"");

        if      (configFileName.endsWith(".properties")) loadProperties       (configFileName);
        else if (configFileName.endsWith(".xml"))        loadPropertiesFromXml(configFileName);
        else
            throw new IllegalArgumentException(String.format("Fichier au format properties (.properties) ou XML (.xml) attendu mais \"%s\" donné", configFileName));
    }

    private void loadProperties(String configFileName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(configFileName)) {
            if (inputStream == null)
                throw new FileNotFoundException(
                    String.format(
                        "Impossible de trouver le fichier de configuration \"%s\". Assurez-vous qu'il existe et qu'il soit accessible dans le classPath de votre application",
                        configFileName
                    )
                );

            properties = new Properties();
            properties.load(inputStream);
            availableDataSources = new HashSet<>();
            for (String propertyName : properties.stringPropertyNames()) {
                propertyName = propertyName.strip();
                String[] propertyNameParts = propertyName.substring(PROPERTIES_KEY_PREFIX.length()).split("\\.");

                Assert.state(propertyNameParts.length == 2,
                    () -> new ConfigurationException(
                        "La déclaration des informations dans un fichier .properties doivent être de la forme : matsd.orm.datasource.<type-de-basededonnées>.<propriété>"
                    )
                );

                if (propertyName.startsWith(PROPERTIES_KEY_PREFIX)) {
                    propertyNameParts[0] = propertyNameParts[0].strip();
                    Assert.state(SUPPORTED_DBMS.contains(propertyNameParts[0]),
                        () -> new UnsupportedDataSourceException(propertyNameParts[0])
                    );

                    properties.setProperty(propertyName, properties.getProperty(propertyName));
                    availableDataSources.add(propertyNameParts[0]);
                }
            }
        } catch (IOException e) {
            throw new ConfigurationException(String.format("Erreur lors de la lecture du fichier de configuration : \"%s\"", configFileName), e);
        }
    }

    private void loadPropertiesFromXml(String xmlConfigFile) {
        properties = new Properties();
        availableDataSources = new HashSet<>();

        for (Map.Entry<String, Map<String, String>> entry : loadXmlConfigs(xmlConfigFile).entrySet()) {
            String datasourceType = entry.getKey();

            Map<String, String> datasourceInfos = entry.getValue();
            for (Map.Entry<String, String> infoEntry : datasourceInfos.entrySet()) {
                String propertyName  = infoEntry.getKey();
                String propertyValue = infoEntry.getValue();

                properties.setProperty(
                    String.format("%s%s.%s", PROPERTIES_KEY_PREFIX, datasourceType, propertyName), propertyValue
                );
            }
            availableDataSources.add(datasourceType);
        }
    }

    private Map<String, Map<String, String>> loadXmlConfigs(String xmlConfigFile) {
        Document document;
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(xmlConfigFile);
            if (inputStream == null)
                throw new FileNotFoundException(
                    String.format(
                        "Impossible de trouver le fichier de configuration XML \"%s\". Assurez-vous qu'il existe et qu'il soit accessible dans le classPath de votre application",
                        xmlConfigFile
                    )
                );

            document = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(inputStream);
        } catch (SAXException | ParserConfigurationException e) {
            throw new XmlParseException(e);
        } catch (IOException e) {
            throw new ConfigurationException(String.format("Erreur lors de la lecture du fichier de configuration : \"%s\"", xmlConfigFile), e);
        }
        document.getDocumentElement().normalize();

        Assert.state(document.getFirstChild().getNodeName().equals("matsd-orm-configuration"),
            () -> new ConfigurationException("L'élément racine dans un fichier de configuration XML doit être \"<matsd-orm-configuration>\"")
        );

        NodeList datasourceNodeList = document.getElementsByTagName("datasource");
        if (datasourceNodeList.getLength() == 0)
            throw new ConfigurationException("Aucune source de données précisées dans le fichier de configuration");

        Map<String, Map<String, String>> configsFromXml = new HashMap<>();
        for (int i = 0; i < datasourceNodeList.getLength(); i++) {
            Node datasourceNode = datasourceNodeList.item(i);

            if (datasourceNode.getNodeType() == Node.ELEMENT_NODE) {
                Element datasourceElement = (Element) datasourceNode;

                NamedNodeMap datasourceAttributes = datasourceElement.getAttributes();
                Assert.state(datasourceAttributes.getLength() == 1 && datasourceAttributes.item(0).getNodeName().strip().equals("type"),
                    () -> new ConfigurationException("L'élément <datasource> doit avoir exactement un attribut \"type\" et aucun autre")
                );

                String datasourceType = datasourceAttributes.item(0).getTextContent().strip();
                if (configsFromXml.containsKey(datasourceType))
                    throw new ConfigurationException(
                        String.format("Duplication détectée pour le type de source de données \"%s\"", datasourceType)
                    );

                if (!SUPPORTED_DBMS.contains(datasourceType.toLowerCase()))
                    throw new UnsupportedDataSourceException(datasourceType);

                configsFromXml.put(datasourceType, extractDataSourceInfos(datasourceElement));
            }
        }

        return configsFromXml;
    }

    private static Map<String, String> extractDataSourceInfos(Element datasourceElement) {
        Map<String, String> datasourceInfos = new HashMap<>();

        NodeList propertyNodeList = datasourceElement.getElementsByTagName("property");
        for (int i = 0; i < propertyNodeList.getLength(); i++) {
            Node propertyNode = propertyNodeList.item(i);

            if (propertyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element propertyElement = (Element) propertyNode;

                NamedNodeMap propertyAttributes = propertyElement.getAttributes();
                if (propertyAttributes.getLength() != 2)
                    throw new ConfigurationException(
                        String.format(
                            "L'élément <property> doit avoir exactement deux (2) attributs : [%s]",
                            String.join(", ", VALID_PROPERTY_ATTRIBUTES)
                        )
                    );

                if (StringUtils.hasText(propertyElement.getTextContent()) || propertyElement.hasChildNodes())
                    throw new ConfigurationException("Textes et balises ne sont plus autorisés entre un élément <property>");

                String propertyName  = null;
                String propertyValue = null;
                for (int j = 0; j < propertyAttributes.getLength(); j++) {
                    Node attributeNode = propertyAttributes.item(j);

                    if (attributeNode.getNodeType() == Node.ATTRIBUTE_NODE) {
                        Attr attribute = (Attr) attributeNode;

                        String attributeName  = attribute.getName();
                        String attributeValue = attribute.getValue();

                        if      (attributeName.equals("name"))  propertyName  = attributeValue.strip();
                        else if (attributeName.equals("value")) propertyValue = attributeValue.strip();
                    }
                }

                if (StringUtils.isBlank(propertyName))
                    throw new ConfigurationException("L'attribut \"name\" de l'élément <property> ne peut pas être vide");

                if (datasourceInfos.containsKey(propertyName))
                    throw new ConfigurationException(String.format("Duplication détectée pour le nom de propriété \"%s\"", propertyName));

                if (!VALID_PROPERTY_NAMES.contains(propertyName))
                    throw new ConfigurationException(String.format("Le nom de propriété \"%s\" n'est pas un nom de propriété valide", propertyName));

                datasourceInfos.put(propertyName, propertyValue);
            }
        }

        return datasourceInfos;
    }
}
