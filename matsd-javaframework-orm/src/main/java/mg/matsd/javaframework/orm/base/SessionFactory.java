package mg.matsd.javaframework.orm.base;

import mg.matsd.javaframework.orm.connection.DatabaseConnector;

public interface SessionFactory {
    DatabaseConnector getDatabaseConnector();

    boolean isShowSql();

    SessionFactory setShowSql(boolean showSql);

    boolean isFormatSql();

    SessionFactory setFormatSql(boolean formatSql);

    Session createSession();
}
