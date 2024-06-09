package mg.itu.prom16.exceptions;

import mg.itu.prom16.base.internal.RequestMappingInfo;
import mg.matsd.javaframework.core.exceptions.BaseException;

public class UndefinedPathVariableException extends BaseException {
    private static final String PREFIX = "Paramètre de chemin non défini";

    public UndefinedPathVariableException(String undefinedPathVariable, RequestMappingInfo requestMappingInfo) {
        super(String.format(
            "Le paramètre \"%s\" ne figure pas parmi la liste des paramètres %s du %s",
            undefinedPathVariable, requestMappingInfo.getPathVariableNames(), requestMappingInfo
        ), PREFIX);
    }
}
