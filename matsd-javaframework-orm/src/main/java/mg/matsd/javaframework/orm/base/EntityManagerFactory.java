package mg.matsd.javaframework.orm.base;

import mg.matsd.javaframework.orm.connection.DatabaseConnector;
import mg.matsd.javaframework.orm.setup.SessionFactoryOptions;

public class EntityManagerFactory implements SessionFactory {
    private final DatabaseConnector databaseConnector;
    private final SessionFactoryOptions sessionFactoryOptions;

    public EntityManagerFactory(SessionFactoryOptions sessionFactoryOptions) {
        this.sessionFactoryOptions = sessionFactoryOptions;
        databaseConnector = sessionFactoryOptions.getDatabaseConnector();
    }

    @Override
    public DatabaseConnector getDatabaseConnector() {
        return databaseConnector;
    }

    public EntityManager createEntityManager() {
        return new EntityManager(databaseConnector);
    }

    @Override
    public Session createSession() {
        return createEntityManager();
    }
}
