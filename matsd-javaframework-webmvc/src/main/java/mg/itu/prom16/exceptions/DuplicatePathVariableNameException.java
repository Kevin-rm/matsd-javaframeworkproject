package mg.itu.prom16.exceptions;

import mg.itu.prom16.base.internal.RequestMappingInfo;
import mg.matsd.javaframework.core.exceptions.BaseException;

public class DuplicatePathVariableNameException extends BaseException {
    public DuplicatePathVariableNameException(
        RequestMappingInfo requestMappingInfo,
        String repeatedPathVariableName
    ) {
        super(String.format(
            "Le nom du paramètre de chemin \"%s\" est dupliqué dans la définition de mapping : %s",
            repeatedPathVariableName, requestMappingInfo)
        );
    }
}
