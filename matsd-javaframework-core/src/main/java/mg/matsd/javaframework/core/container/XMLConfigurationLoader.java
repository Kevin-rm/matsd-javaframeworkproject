package mg.matsd.javaframework.core.container;

import mg.matsd.javaframework.core.io.Resource;
import mg.matsd.javaframework.core.managedinstances.ManagedInstance;
import mg.matsd.javaframework.core.managedinstances.ManagedInstanceUtils;
import mg.matsd.javaframework.core.managedinstances.factory.ManagedInstanceFactory;
import org.w3c.dom.Element;

import java.lang.reflect.Constructor;
import java.util.List;

import static mg.matsd.javaframework.core.utils.XMLUtils.*;

class XMLConfigurationLoader {
    private XMLConfigurationLoader() { }

    static void doLoadManagedInstances(ManagedInstanceFactory managedInstanceFactory, Resource resource) {
        Element documentElement = buildValidatedDocument(XMLConfigurationLoader.class.getClassLoader(), resource,
            "container.xsd", "managedinstances.xsd").getDocumentElement();

        scanComponents(managedInstanceFactory, documentElement);

        List<Element> elements = getChildElementsByTagName(documentElement, "managed-instance");
        if (elements.size() == 0) return;

        for (Element element : elements) {
            ManagedInstance managedInstance = new ManagedInstance(
                getElementAttributeValue(element, "id"),
                getElementAttributeValue(element, "class"),
                getElementAttributeValue(element, "scope")
            );
            managedInstanceFactory.registerManagedInstance(managedInstance);

            for (Element childElement : getChildElements(element)) {
                String childElementTagName = childElement.getTagName();
                if (childElementTagName.equals("constructor-arg")) addConstructorArguments(managedInstance, childElement);
                else if (childElementTagName.equals("property"))   addProperties(managedInstance, childElement);
            }
        }
    }

    private static void scanComponents(ManagedInstanceFactory managedInstanceFactory, Element documentElement) {
        Element firstChildElementByTagName = getFirstChildElementByTagName(documentElement, "container:component-scan");
        if (firstChildElementByTagName == null) return;

        managedInstanceFactory.setComponentScanBasePackage(getElementAttributeValue(firstChildElementByTagName, "base-package"))
            .scanComponents();
    }

    private static void addConstructorArguments(ManagedInstance managedInstance, Element element) {
        Constructor<?> constructor = ManagedInstanceUtils.constructorToUse(managedInstance);

        String constructorArgValue = getElementAttributeValue(element, "value");
        managedInstance.addConstructorArgument(
            getElementAttributeValue(element, "index"),
            constructorArgValue == null ? element.getTextContent() : constructorArgValue,
            getElementAttributeValue(element, "ref"), constructor);
    }

    private static void addProperties(ManagedInstance managedInstance, Element element) {
        String propertyValue = getElementAttributeValue(element, "value");
        managedInstance.addProperty(
            getElementAttributeValue(element, "name"),
            propertyValue == null ? element.getTextContent() : propertyValue,
            getElementAttributeValue(element, "ref")
        );
    }
}
