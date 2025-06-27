package mg.matsd.javaframework.di.exceptions;

import mg.matsd.javaframework.core.annotations.metadata.Nullable;
import mg.matsd.javaframework.core.exceptions.BaseException;
import mg.matsd.javaframework.core.utils.StringUtils;

public class InvalidPackageException extends BaseException {
    private static final String PREFIX = "Nom de package invalide";
    private final String invalidPackage;

    public InvalidPackageException(@Nullable final String message, final String invalidPackage) {
        super(StringUtils.isNullOrBlank(message) ?
            String.format("\"%s\" n'est pas un package valide", invalidPackage) : message, PREFIX);
        this.invalidPackage = invalidPackage;
    }

    public String getInvalidPackage() {
        return invalidPackage;
    }
}
