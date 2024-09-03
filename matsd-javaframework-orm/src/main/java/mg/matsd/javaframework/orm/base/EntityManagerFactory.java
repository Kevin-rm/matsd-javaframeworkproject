package mg.matsd.javaframework.orm.base;

import mg.matsd.javaframework.orm.connection.DatabaseConnector;

public class EntityManagerFactory  {
    private final DatabaseConnector databaseConnector;
    private boolean showSql;
    private boolean formatSql;

    EntityManagerFactory(DatabaseConnector databaseConnector, boolean showSql, boolean formatSql) {
        this.databaseConnector = databaseConnector;
        this.setShowSql(showSql)
            .setFormatSql(formatSql);
    }

    public boolean isShowSql() {
        return showSql;
    }

    public EntityManagerFactory setShowSql(boolean showSql) {
        this.showSql = showSql;
        return this;
    }

    public boolean isFormatSql() {
        return formatSql;
    }

    public EntityManagerFactory setFormatSql(boolean formatSql) {
        this.formatSql = formatSql;
        return this;
    }
}
