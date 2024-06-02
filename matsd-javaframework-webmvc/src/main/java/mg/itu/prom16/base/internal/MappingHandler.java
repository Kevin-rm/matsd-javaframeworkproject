package mg.itu.prom16.base.internal;

import com.sun.jdi.InternalException;
import mg.itu.prom16.base.ModelView;
import mg.matsd.javaframework.core.utils.Assert;

import java.lang.reflect.InvocationTargetException;
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

    private MappingHandler setControllerClass(Class<?> controllerClass) {
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

    private MappingHandler setMethod(Method method) {
        Assert.notNull(method, "La méthode associée au path ne peut pas être \"null\"");
        Assert.state(method.getDeclaringClass() == controllerClass,
            () -> new IllegalArgumentException(
                String.format("Le contrôleur \"%s\" ne dispose pas d'une méthode nommée \"%s\"", controllerClass.getName(), method.getName())
            ));

        this.method = method;
        return this;
    }

    public Object invokeMethod(Object controllerInstance) {
        Assert.notNull(controllerInstance);

        if (controllerInstance.getClass() != controllerClass)
            throw new InternalException();

        try {
            if (method.getReturnType() == ModelView.class)
                return method.invoke(controllerInstance);

            return method.invoke(controllerInstance).toString();
        } catch (IllegalAccessException e) {
            throw new InternalException();
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "MappingHandler{" +
            "controllerClass=" + controllerClass +
            ", method=" + method +
            '}';
    }
}
