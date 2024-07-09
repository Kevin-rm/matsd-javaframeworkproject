package mg.itu.prom16.exceptions;

import mg.itu.prom16.base.internal.RequestMappingInfo;
import mg.matsd.javaframework.core.exceptions.BaseException;

public class UndefinedPathVariableException extends BaseException {
    private static final String PREFIX = "Paramètre de chemin non défini";

    public UndefinedPathVariableException(String undefinedPathVariable, RequestMappingInfo requestMappingInfo) {
        super(String.format(
            "Le paramètre de chemin \"%s\" ne figure pas parmi la liste des paramètres disponibles %s du %s",
            undefinedPathVariable, requestMappingInfo.getPathVariablesAttributes().keySet(), requestMappingInfo
        ), PREFIX);
    }
}
