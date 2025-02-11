package mg.matsd.javaframework.core.exceptions;

public class PackageNotFoundException extends BaseException {
    private final String packageName;

    public PackageNotFoundException(final String packageName) {
        super(String.format("Le package \"%s\" est soit introuvable, soit inaccessible dans le classpath " +
            "ou soit ne correspond pas à un dossier", packageName));

        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }
}
