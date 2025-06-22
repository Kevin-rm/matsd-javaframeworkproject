package mg.matsd.javaframework.di.container;

import mg.matsd.javaframework.core.env.DefaultPropertyHolder;
import mg.matsd.javaframework.core.io.Resource;
import mg.matsd.javaframework.di.managedinstances.ManagedInstance;
import mg.matsd.javaframework.di.managedinstances.ManagedInstanceUtils;
import mg.matsd.javaframework.di.managedinstances.factory.ManagedInstanceFactory;
import org.w3c.dom.Element;

import java.lang.reflect.Constructor;
import java.util.List;

import static mg.matsd.javaframework.core.utils.XMLUtils.*;

class XMLConfigurationLoader {
    private XMLConfigurationLoader() { }

    static void doLoadManagedInstances(AbstractXmlResourceContainer abstractXmlResourceContainer, Resource resource) {
        Element documentElement = buildDocumentElement(XMLConfigurationLoader.class.getClassLoader(), resource,
            abstractXmlResourceContainer.getSchemas());

        setEnvironment(abstractXmlResourceContainer, documentElement);
        scanComponents(abstractXmlResourceContainer, documentElement);
        abstractXmlResourceContainer.additionalXmlConfigLoadingLogic(documentElement);

        List<Element> elements = getChildElementsByTagName(documentElement, "managed-instance");
        if (elements.isEmpty()) return;

        for (Element element : elements) {
            ManagedInstance managedInstance = new ManagedInstance(
                getAttributeValue(element, "id"),
                getAttributeValue(element, "class"),
                getAttributeValue(element, "scope"));
            abstractXmlResourceContainer.registerManagedInstance(managedInstance);

            for (Element childElement : getChildElements(element)) {
                String childElementTagName = childElement.getTagName();
                if (childElementTagName.equals("constructor-arg")) addConstructorArguments(managedInstance, childElement);
                else if (childElementTagName.equals("property"))   addProperties(managedInstance, childElement);
            }
        }
    }

    private static void setEnvironment(ManagedInstanceFactory managedInstanceFactory, Element documentElement) {
        Element element = getFirstChildElementByTagName(documentElement, "property-source");

        managedInstanceFactory.setEnvironment(element != null ?
            new DefaultPropertyHolder(getAttributeValue(element, "location")) : null);
    }

    private static void scanComponents(ManagedInstanceFactory managedInstanceFactory, Element documentElement) {
        Element element = getFirstChildElementByTagName(documentElement, "container:component-scan");
        if (element == null) return;

        managedInstanceFactory.setComponentScanBasePackage(getAttributeValue(element, "base-package"))
            .scanComponents();
    }

    private static void addConstructorArguments(ManagedInstance managedInstance, Element element) {
        Constructor<?> constructor = ManagedInstanceUtils.constructorToUse(managedInstance);

        String constructorArgValue = getAttributeValue(element, "value");
        managedInstance.addConstructorArgument(
            getAttributeValue(element, "index"),
            constructorArgValue == null ? element.getTextContent() : constructorArgValue,
            getAttributeValue(element, "ref"), constructor);
    }

    private static void addProperties(ManagedInstance managedInstance, Element element) {
        String propertyValue = getAttributeValue(element, "value");
        managedInstance.addProperty(
            getAttributeValue(element, "name"),
            propertyValue == null ? element.getTextContent() : propertyValue,
            getAttributeValue(element, "ref")
        );
    }
}
