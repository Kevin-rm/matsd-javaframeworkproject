package mg.matsd.javaframework.core.io;

import java.io.InputStream;

public class ClassPathResource extends Resource {
    public ClassPathResource(String name) {
        super(name);
    }

    @Override
    protected void initializeInputStream() throws ResourceNotFoundException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(name);
        if (inputStream == null)
            throw new ResourceNotFoundException(
                String.format(
                    "Impossible de trouver le fichier \"%s\". Assurez-vous qu'il existe et qu'il soit accessible dans le classPath de votre application",
                    name
                )
            );

        this.inputStream = inputStream;
    }
}
