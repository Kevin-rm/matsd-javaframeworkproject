package mg.matsd.javaframework.orm.exceptions;

import mg.matsd.javaframework.core.exceptions.BaseException;

import java.util.Set;

public class DataSourceNotFoundException extends BaseException {
    private static final String PREFIX = "Source de données introuvable dans les configurations";

    public DataSourceNotFoundException(String unrecognizedDataSource, Set<String> availableDataSources) {
        super(String.format("\"%s\". Voici la liste des sources de données disponibles [%s]",
            unrecognizedDataSource, String.join(", ", availableDataSources)), PREFIX
        );
    }
}
