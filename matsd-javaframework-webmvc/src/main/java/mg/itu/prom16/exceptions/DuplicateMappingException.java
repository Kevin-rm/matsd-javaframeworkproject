package mg.itu.prom16.exceptions;

import mg.itu.prom16.base.internal.RequestMappingInfo;
import mg.matsd.javaframework.core.exceptions.BaseException;

import java.util.Arrays;

public class DuplicateMappingException extends BaseException {
    private static final String PREFIX = "Configuration de mappage en double détectée";

    public DuplicateMappingException(RequestMappingInfo requestMappingInfo) {
        super(
            String.format("path : \"%s\", methods : %s",
                requestMappingInfo.getPath(), Arrays.toString(requestMappingInfo.getMethods())
            ), PREFIX);
    }
}
