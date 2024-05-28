package mg.itu.prom16.base.internal;

import mg.matsd.javaframework.core.utils.Assert;

import java.lang.reflect.Method;

public class MappingHandler {
    private Class<?> controllerClass;
    private Method   method;

    public MappingHandler(Class<?> controllerClass, Method method) {
        this.setControllerClass(controllerClass)
            .setMethod(method);
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public MappingHandler setControllerClass(Class<?> controllerClass) {
        Assert.notNull(controllerClass, "La classe du contrôleur ne peut pas être \"null\"");
        Assert.state(UtilFunctions.isController(controllerClass),
            () -> new IllegalArgumentException("La classe passée en argument n'est pas un contrôleur")
        );

        this.controllerClass = controllerClass;
        return this;
    }

    public Method getMethod() {
        return method;
    }

    public MappingHandler setMethod(Method method) {
        Assert.notNull(method, "La méthode associée au path ne peut pas être \"null\"");
        Assert.state(method.getDeclaringClass() == controllerClass,
            () -> new IllegalArgumentException(
                String.format("Le contrôleur \"%s\" ne dispose pas d'une méthode nommée \"%s\"", controllerClass.getName(), method.getName())
            ));

        this.method = method;
        return this;
    }

    @Override
    public String toString() {
        return "MappingHandler{" +
            "controllerClass=" + controllerClass +
            ", method=" + method +
            '}';
    }
}
