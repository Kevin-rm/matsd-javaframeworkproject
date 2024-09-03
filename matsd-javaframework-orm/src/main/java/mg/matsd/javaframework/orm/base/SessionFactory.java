package mg.matsd.javaframework.orm.base;

import mg.matsd.javaframework.orm.connection.DatabaseConnector;

public interface SessionFactory {
    DatabaseConnector getDatabaseConnector();

    boolean isShowSql();

    EntityManagerFactory setShowSql(boolean showSql);

    boolean isFormatSql();

    EntityManagerFactory setFormatSql(boolean formatSql);
}
