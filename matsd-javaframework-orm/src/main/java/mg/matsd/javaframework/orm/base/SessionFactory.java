package mg.matsd.javaframework.orm.base;

import mg.matsd.javaframework.orm.connection.DatabaseConnector;
import mg.matsd.javaframework.orm.setup.Configuration;

public interface SessionFactory {
    Configuration getConfiguration();

    String getDialect();

    DatabaseConnector getDatabaseConnector();

    Session createSession();
}
