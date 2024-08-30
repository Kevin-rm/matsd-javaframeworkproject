package mg.matsd.javaframework.orm.exceptions;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.exceptions.BaseException;
import mg.matsd.javaframework.core.utils.StringUtils;

public class UnsupportedDataSourceException extends BaseException {
    public UnsupportedDataSourceException(@Nullable String unsupportedDataSource) {
        super(formatMessage(unsupportedDataSource));
    }

    private static String formatMessage(@Nullable String unsupportedDataSource) {
        if (StringUtils.isBlank(unsupportedDataSource))
            return "";

        return String.format("Le type de source de données \"%s\" n'est pas pris en charge par ce programme", unsupportedDataSource);
    }
}
