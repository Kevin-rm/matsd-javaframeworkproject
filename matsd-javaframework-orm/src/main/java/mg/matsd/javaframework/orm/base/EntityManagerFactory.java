package mg.matsd.javaframework.orm.base;

import mg.matsd.javaframework.orm.connection.DatabaseConnector;
import mg.matsd.javaframework.orm.setup.SessionFactoryOptions;

public class EntityManagerFactory implements SessionFactory {
    private final SessionFactoryOptions sessionFactoryOptions;
    private final DatabaseConnector     databaseConnector;

    public EntityManagerFactory(SessionFactoryOptions sessionFactoryOptions) {
        this.sessionFactoryOptions = sessionFactoryOptions;
        databaseConnector = sessionFactoryOptions.getDatabaseConnector();
    }

    SessionFactoryOptions getSessionFactoryOptions() {
        return sessionFactoryOptions;
    }

    @Override
    public DatabaseConnector getDatabaseConnector() {
        return databaseConnector;
    }

    public EntityManager createEntityManager() {
        return new EntityManager(this);
    }

    @Override
    public Session createSession() {
        return createEntityManager();
    }
}
