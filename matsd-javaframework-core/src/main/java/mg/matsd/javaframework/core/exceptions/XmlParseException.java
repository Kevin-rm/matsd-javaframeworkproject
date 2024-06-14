package mg.matsd.javaframework.core.exceptions;

public class XmlParseException extends BaseException {
    private static final String PREFIX = "Erreur lors de l'analyse de fichier XML";

    public XmlParseException(Throwable cause) {
        super(PREFIX, cause);
    }
}
