package mg.matsd.javaframework.orm.proxy;

import mg.matsd.javaframework.orm.mapping.Entity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class LazyLoader implements InvocationHandler {
    private final Object target;
    private final Entity entity;

    public LazyLoader(Object target, Entity entity) {
        this.target = target;
        this.entity = entity;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(proxy, args);
    }

    public static Object createProxy(Object target, Entity entity) {
        return Proxy.newProxyInstance(
            entity.getClazz().getClassLoader(),
            entity.getClazz().getInterfaces(),
            new LazyLoader(target, entity)
        );
    }
}
