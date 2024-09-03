package mg.matsd.javaframework.orm.base;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.orm.connection.DatabaseConnector;
import mg.matsd.javaframework.orm.exceptions.DataSourceNotFoundException;
import mg.matsd.javaframework.orm.setup.Configuration;

import java.util.Properties;
import java.util.Set;

public class EntityManagerFactory  {
    private Configuration configuration;
    private DatabaseConnector databaseConnector;
    private boolean showSql;
    private boolean formatSql;



}
