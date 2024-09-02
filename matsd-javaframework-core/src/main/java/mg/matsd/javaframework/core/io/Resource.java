package mg.matsd.javaframework.core.io;

import mg.matsd.javaframework.core.utils.Assert;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public abstract class Resource implements Closeable {
    protected final String name;
    protected InputStream inputStream;
    private boolean isClosed = false;

    protected Resource(String name) {
        Assert.notBlank(name, false, "Le nom d'une ressource ne peut pas être vide ou \"null\"");

        this.name = name.strip();
    }

    public String getName() {
        return name;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public boolean isClosed() {
        return isClosed;
    }

    protected abstract void initializeInputStream() throws ResourceNotFoundException;

    @Override
    public void close() {
        if (inputStream == null || isClosed()) return;

        try {
            inputStream.close();
            isClosed = true;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la fermeture de l'\"inputStream\"");
        } finally {
            inputStream = null;
        }
    }
}
