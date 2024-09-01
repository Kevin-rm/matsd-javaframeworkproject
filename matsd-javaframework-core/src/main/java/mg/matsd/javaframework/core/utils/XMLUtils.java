package mg.matsd.javaframework.core.utils;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.exceptions.XmlParseException;
import mg.matsd.javaframework.core.io.ClassPathResource;
import mg.matsd.javaframework.core.io.Resource;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class XMLUtils {
    private static final SchemaFactory SCHEMA_FACTORY = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    private XMLUtils() { }

    public static Element buildDocumentElement(ClassLoader classLoader, Resource resource, String... schemas) {
        Assert.notNull(classLoader, "L'argument classLoader ne peut pas être \"null\"");
        Assert.notNull(resource, "L'argument resource ne peut pas être \"null\"");
        Assert.state(!resource.isClosed(), "Impossible d'utiliser la ressource car elle est déjà fermée");
        Assert.notNull(schemas, "L'argument schemas ne peut pas être \"null\"");
        Arrays.stream(schemas).forEach(schema -> {
            if (schema == null)
                throw new IllegalArgumentException("Chaque élément de schemas ne peut pas être \"null\"");
            if (!schema.endsWith(".xsd"))
                throw new IllegalArgumentException(String.format(
                    "Le schéma XML \"%s\" est invalide car il n'a pas l'extension \".xsd\"", schema));
        });

        Document document;
        try {
            document = createDocumentBuilderFactory(schemas)
                .newDocumentBuilder()
                .parse(resource.getInputStream());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new XmlParseException(e);
        }
        Element documentElement = document.getDocumentElement();
        documentElement.normalize();

        return documentElement;
    }

    public static List<Element> getChildElements(Element parentElement) {
        Assert.notNull(parentElement, "L'argument parentElement ne peut pas être \"null\"");

        return filterElementsFromNodeList(parentElement.getChildNodes());
    }

    public static List<Element> getChildElementsByTagName(Element parentElement, String childElementTagName) {
        Assert.notNull(parentElement, "L'argument parentElement ne peut pas être \"null\"");
        Assert.notBlank(childElementTagName, false, "L'argument childElementTagName ne peut pas être vide ou \"null\"");

        return filterElementsFromNodeList(parentElement.getElementsByTagName(childElementTagName));
    }

    @Nullable
    public static Element getFirstChildElementByTagName(Element parentElement, String childElementTagName) {
        List<Element> childElements = getChildElementsByTagName(parentElement, childElementTagName);
        if (childElements.size() == 0) return null;

        return childElements.get(0);
    }

    @Nullable
    public static String getAttributeValue(Element element, String attributeName) {
        Assert.notNull(element, "L'argument element ne peut pas être \"null\"");
        Assert.notBlank(attributeName, false, "L'argument attributeName ne peut pas être vide ou \"null\"");

        Attr attribute = element.getAttributeNode(attributeName);
        return attribute == null ? null : attribute.getValue();
    }

    private static List<Element> filterElementsFromNodeList(NodeList nodeList) {
        return IntStream.range(0, nodeList.getLength())
            .mapToObj(nodeList::item)
            .filter(node -> node.getNodeType() == Node.ELEMENT_NODE)
            .map(node -> (Element) node)
            .collect(Collectors.toList());
    }

    private static DocumentBuilderFactory createDocumentBuilderFactory(String... schemas) throws SAXException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);

        Schema schema = SCHEMA_FACTORY.newSchema(
            Arrays.stream(schemas)
                .map(schemaName -> {
                    try (Resource r = new ClassPathResource(schemaName)) {
                        return new StreamSource(r.getInputStream());
                    }
                })
                .toArray(Source[]::new)
        );
        documentBuilderFactory.setSchema(schema);

        return documentBuilderFactory;
    }
}
