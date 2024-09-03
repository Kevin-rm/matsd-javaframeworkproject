package mg.matsd.javaframework.orm.base;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.exceptions.TypeMismatchException;
import mg.matsd.javaframework.core.utils.converter.StringConverter;
import mg.matsd.javaframework.orm.connection.DatabaseConnector;

public class EntityManagerFactory implements SessionFactory {
    private final DatabaseConnector databaseConnector;
    private boolean showSql;
    private boolean formatSql;

    EntityManagerFactory(DatabaseConnector databaseConnector, String showSql, String formatSql) {
        this.databaseConnector = databaseConnector;
        this.setShowSql(showSql)
            .setFormatSql(formatSql);
    }

    @Override
    public DatabaseConnector getDatabaseConnector() {
        return databaseConnector;
    }

    @Override
    public boolean isShowSql() {
        return showSql;
    }

    @Override
    public EntityManagerFactory setShowSql(boolean showSql) {
        this.showSql = showSql;
        return this;
    }

    private EntityManagerFactory setShowSql(@Nullable String showSql) {
        if (showSql == null) return this;

        try {
            return setShowSql(StringConverter.convert(showSql, boolean.class));
        } catch (TypeMismatchException e) {
            throw new IllegalArgumentException(String.format("La valeur de la propriété \"showSql\" " +
                "n'est pas de type boolean : \"%s\"", showSql));
        }
    }

    @Override
    public boolean isFormatSql() {
        return formatSql;
    }

    @Override
    public EntityManagerFactory setFormatSql(boolean formatSql) {
        this.formatSql = formatSql;
        return this;
    }

    private EntityManagerFactory setFormatSql(@Nullable String formatSql) {
        if (formatSql == null) return this;

        try {
            return setFormatSql(StringConverter.convert(formatSql, boolean.class));
        } catch (TypeMismatchException e) {
            throw new IllegalArgumentException(String.format("La valeur de la propriété \"formatSql\" " +
                "n'est pas de type boolean : \"%s\"", formatSql));
        }
    }
}
