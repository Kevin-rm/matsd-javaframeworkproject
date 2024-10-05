package mg.matsd.javaframework.orm.proxy;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.orm.mapping.Column;
import mg.matsd.javaframework.orm.mapping.Entity;
import mg.matsd.javaframework.orm.mapping.JoinColumn;
import mg.matsd.javaframework.orm.mapping.Relationship;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.stream.IntStream;

public class LazyLoader implements InvocationHandler {
    private static final String SQL_TEMPLATE = "SELECT %s FROM %s %s %s WHERE %s";

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
        Entity targetEntity = relationship.getTargetEntity();

        String projections = buildProjectionSql(targetEntity);
        

    }

    private static String buildProjectionSql(Entity entity) {
        StringBuilder stringBuilder = new StringBuilder();

        List<Column> columns = entity.getColumns();
        int columnsSize = columns.size();
        IntStream.range(0, columnsSize).forEachOrdered(i -> {
            stringBuilder.append(String.format("%s", columns.get(i).getName()));
            if (i != columnsSize - 1) stringBuilder.append(", ");
        });

        return stringBuilder.toString();
    }

    private static String buildJoinSql(Relationship relationship) {
        StringBuilder stringBuilder = new StringBuilder();

        if (relationship.isToMany()) {
            stringBuilder.append("JOIN ").append("%s AS e_2").append(" ON ");
            List<JoinColumn> joinColumns = relationship.getJoinColumns();
            for (int i = 0; i < joinColumns.size(); i++) {
                JoinColumn joinColumn = joinColumns.get(i);
                stringBuilder.append(joinColumn.getName()).append(" = ").append(joinColumn.getReferencedColumn()).append(" ");
                if (i < joinColumns.size() - 1)
                    stringBuilder.append("AND ");
            }
        } else {
            List<JoinColumn> joinColumns = relationship.getJoinColumns();
            for (JoinColumn joinColumn : joinColumns) {
                stringBuilder.append("JOIN ").append(relationship.getTargetEntity().getTableName())
                        .append(" ON ").append(joinColumn.getName()).append(" = ").append(joinColumn.getReferencedColumn()).append(" ");
            }
        }

        return stringBuilder.toString();
    }
}
