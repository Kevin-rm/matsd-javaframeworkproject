package mg.matsd.javaframework.orm.exceptions;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.exceptions.BaseException;
import mg.matsd.javaframework.core.utils.StringUtils;

public class UnsupportedDatasourceException extends BaseException {
    public UnsupportedDatasourceException(@Nullable String unsupportedDatasource) {
        super(StringUtils.isBlank(unsupportedDatasource) ? "" :
            String.format("Le type de source de données \"%s\" n'est pas pris en charge par ce programme", unsupportedDatasource));
    }
}
