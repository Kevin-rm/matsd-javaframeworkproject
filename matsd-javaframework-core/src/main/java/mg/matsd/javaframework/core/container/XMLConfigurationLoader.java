package mg.matsd.javaframework.core.container;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.exceptions.XmlParseException;
import mg.matsd.javaframework.core.io.Resource;
import mg.matsd.javaframework.core.managedinstances.ManagedInstance;
import mg.matsd.javaframework.core.managedinstances.ManagedInstanceUtils;
import mg.matsd.javaframework.core.managedinstances.factory.ManagedInstanceFactory;
import mg.matsd.javaframework.core.utils.StringUtils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.lang.reflect.Constructor;

class XMLConfigurationLoader {
    private XMLConfigurationLoader() { }

    static void doLoadManagedInstances(ManagedInstanceFactory managedInstanceFactory, Resource resource) {
        Document document;
        ClassLoader classLoader = XMLConfigurationLoader.class.getClassLoader();

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setSchema(
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new Source[] {
                    new StreamSource(classLoader.getResourceAsStream("container.xsd")),
                    new StreamSource(classLoader.getResourceAsStream("managedinstances.xsd"))
                })
            );

            document = documentBuilderFactory
                .newDocumentBuilder()
                .parse(resource.getInputStream());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new XmlParseException(e);
        }
        document.getDocumentElement().normalize();

        scanComponents(managedInstanceFactory, document);

        NodeList managedInstanceNodeList = document.getElementsByTagName("managed-instance");
        if (managedInstanceNodeList.getLength() == 0) return;

        for (int i = 0; i < managedInstanceNodeList.getLength(); i++) {
            Node managedInstanceNode = managedInstanceNodeList.item(i);

            if (managedInstanceNode.getNodeType() == Node.ELEMENT_NODE) {
                Element managedInstanceElement = (Element) managedInstanceNode;

                ManagedInstance managedInstance = new ManagedInstance(
                    getElementAttributeValue(managedInstanceElement, "id"),
                    getElementAttributeValue(managedInstanceElement, "class"),
                    getElementAttributeValue(managedInstanceElement, "scope")
                );
                managedInstanceFactory.registerManagedInstance(managedInstance);

                addConstructorArguments(managedInstance, managedInstanceElement);
                addProperties(managedInstance, managedInstanceElement);
            }
        }
    }

    @Nullable
    private static String getElementAttributeValue(Element element, String name) {
        Attr attribute = element.getAttributeNode(name);

        return attribute == null ? null : attribute.getValue();
    }

    private static void scanComponents(ManagedInstanceFactory managedInstanceFactory, Document document) {
        NodeList nodeList = document.getElementsByTagName("container:component-scan");
        if (nodeList.getLength() == 0) return;

        managedInstanceFactory.setComponentScanBasePackage(
            getElementAttributeValue((Element) nodeList.item(0), "base-package")
        ).scanComponents();
    }

    private static void addConstructorArguments(ManagedInstance managedInstance, Element managedInstanceElement) {
        NodeList constructorArgNodeList = managedInstanceElement.getElementsByTagName("constructor-arg");
        if (constructorArgNodeList.getLength() == 0) return;

        Constructor<?> constructor = ManagedInstanceUtils.constructorToUse(managedInstance);
        for (int i = 0; i < constructorArgNodeList.getLength(); i++) {
            Node constructorArgNode = constructorArgNodeList.item(i);

            if (constructorArgNode.getNodeType() == Node.ELEMENT_NODE) {
                Element constructorArgElement = (Element) constructorArgNode;

                managedInstance.addConstructorArgument(
                    getElementAttributeValue(constructorArgElement, "index"),
                    getValue(constructorArgElement),
                    getElementAttributeValue(constructorArgElement, "ref"),
                    constructor
                );
            }
        }
    }

    private static void addProperties(ManagedInstance managedInstance, Element managedInstanceElement) {
        NodeList propertyNodeList = managedInstanceElement.getElementsByTagName("property");
        if (propertyNodeList.getLength() == 0) return;

        for (int i = 0; i < propertyNodeList.getLength(); i++) {
            Node propertyNode = propertyNodeList.item(i);

            if (propertyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element propertyElement = (Element) propertyNode;

                managedInstance.addProperty(
                    getElementAttributeValue(propertyElement, "name"),
                    getValue(propertyElement),
                    getElementAttributeValue(propertyElement, "ref")
                );
            }
        }
    }

    @Nullable
    private static String getValue(Element element) {
        String value = getElementAttributeValue(element, "value");

        String elementTextContent = element.getTextContent();
        if (value == null && StringUtils.hasText(elementTextContent))
            return elementTextContent;

        return value;
    }
}
