package mg.matsd.javaframework.core.container;

import mg.matsd.javaframework.core.io.Resource;
import mg.matsd.javaframework.core.managedinstances.ManagedInstance;
import mg.matsd.javaframework.core.managedinstances.ManagedInstanceUtils;
import mg.matsd.javaframework.core.managedinstances.factory.ManagedInstanceFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.lang.reflect.Constructor;
import java.util.List;

import static mg.matsd.javaframework.core.utils.XMLUtils.*;

class XMLConfigurationLoader {
    private XMLConfigurationLoader() { }

    static void doLoadManagedInstances(ManagedInstanceFactory managedInstanceFactory, Resource resource) {
        Document document = buildValidatedDocument(XMLConfigurationLoader.class.getClassLoader(), resource,
            "container.xsd", "managedinstances.xsd");

        scanComponents(managedInstanceFactory, document);

        List<Element> elements = getChildElementsByTagName(document, "managed-instance");
        if (elements.size() == 0) return;

        for (Element element : elements) {
            ManagedInstance managedInstance = new ManagedInstance(
                getElementAttributeValue(element, "id"),
                getElementAttributeValue(element, "class"),
                getElementAttributeValue(element, "scope")
            );
            managedInstanceFactory.registerManagedInstance(managedInstance);

            addConstructorArguments(managedInstance, element);
            addProperties(managedInstance, element);
        }
    }

    private static void scanComponents(ManagedInstanceFactory managedInstanceFactory, Document document) {
        Element element = getFirstChildElementByTagName(document, "container:component-scan");
        if (element == null) return;

        managedInstanceFactory.setComponentScanBasePackage(getElementAttributeValue(element, "base-package"))
            .scanComponents();
    }

    private static void addConstructorArguments(ManagedInstance managedInstance, Element parentElement) {
        List<Element> elements = getChildElementsByTagName(parentElement, "constructor-arg");
        if (elements == null) return;

        Constructor<?> constructor = ManagedInstanceUtils.constructorToUse(managedInstance);
        for (Element element : elements) {
            String constructorArgValue = getElementAttributeValue(element, "value");

            managedInstance.addConstructorArgument(
                getElementAttributeValue(element, "index"),
                constructorArgValue == null ? element.getTextContent() : constructorArgValue,
                getElementAttributeValue(element, "ref"), constructor);
        }
    }

    private static void addProperties(ManagedInstance managedInstance, Element parentElement) {
        List<Element> elements = getChildElementsByTagName(parentElement, "property");
        if (elements == null) return;

        for (Element element : elements) {
            String propertyValue = getElementAttributeValue(element, "value");

            managedInstance.addProperty(
                getElementAttributeValue(element, "name"),
                propertyValue == null ? element.getTextContent() : propertyValue,
                getElementAttributeValue(element, "ref")
            );
        }
    }
}
