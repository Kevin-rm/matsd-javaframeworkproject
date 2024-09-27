package mg.matsd.javaframework.core.utils.converter;

import mg.matsd.javaframework.core.exceptions.BaseException;

class UnsupportedTypeConversionException extends BaseException {
    private static final String PREFIX = "Conversion non pris en charge de \"String\" vers le type";

    UnsupportedTypeConversionException(Class<?> type) {
        super(String.format("%s", type.getName()), PREFIX);
    }
}
