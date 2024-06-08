package mg.matsd.javaframework.core.utils.converter;

import mg.matsd.javaframework.core.exceptions.BaseException;

class UnsupportedConversionTypeException extends BaseException {
    private static final String PREFIX = "Conversion en \"String\" non pris en charge pour le type";

    UnsupportedConversionTypeException(Class<?> type) {
        super(String.format("%s", type.getName()), PREFIX);
    }
}
