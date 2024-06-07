package mg.matsd.javaframework.core.exceptions;

public class PackageNotFoundException extends BaseException {
    public PackageNotFoundException(String packageName) {
        super(String.format(
            "Le package \"%s\" est soit introuvable, soit inaccessible dans le classpath " +
            "ou soit ne correspond pas à un dossier", packageName)
        );
    }
}
