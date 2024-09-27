package mg.matsd.javaframework.orm.base;

import mg.matsd.javaframework.orm.connection.DatabaseConnector;

public interface SessionFactory {
    DatabaseConnector getDatabaseConnector();

    Session createSession();
}
