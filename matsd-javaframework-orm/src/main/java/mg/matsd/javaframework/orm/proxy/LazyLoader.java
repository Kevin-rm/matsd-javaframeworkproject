package mg.matsd.javaframework.orm.proxy;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.orm.mapping.Entity;
import mg.matsd.javaframework.orm.mapping.Relationship;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class LazyLoader implements InvocationHandler {
    private static final String SQL_TEMPLATE = "SELECT %s FROM %s WHERE %";

    private final Object target;
    private final Entity entity;

    public LazyLoader(Object target, Entity entity) {
        this.target = target;
        this.entity = entity;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Relationship relationship = getLazyLoadedRelationship(method);
        if (relationship != null) loadLazyRelationship(relationship);

        return method.invoke(proxy, args);
    }

    public static Object createProxy(Object target, Entity entity) {
        Class<?> entityClass = entity.getClazz();

        return Proxy.newProxyInstance(
            entityClass.getClassLoader(),
            entityClass.getInterfaces(),
            new LazyLoader(target, entity)
        );
    }

    @Nullable
    private Relationship getLazyLoadedRelationship(Method method) {
        String methodName = method.getName();

        if (!methodName.startsWith("get")) return null;
        String fieldName = StringUtils.firstLetterToLower(methodName.substring(3));

        return entity.getRelationships().stream()
            .filter(relationship -> relationship.getField().getName().equals(fieldName))
            .findFirst()
            .orElse(null);
    }

    private void loadLazyRelationship(Relationship relationship) {

    }
}
