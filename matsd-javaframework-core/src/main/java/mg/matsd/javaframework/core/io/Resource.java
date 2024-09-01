package mg.matsd.javaframework.core.io;

import com.sun.jdi.InternalException;
import mg.matsd.javaframework.core.utils.Assert;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public abstract class Resource implements Closeable {
    protected String      name;
    protected InputStream inputStream;
    private boolean isClosed;

    protected Resource(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        Assert.notBlank(name, false, "Le nom d'une ressource ne peut pas être vide ou \"null\"");

        this.name = name.strip();
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
        if (inputStream == null) return;

        try {
            inputStream.close();
            isClosed = true;
        } catch (IOException ignored) {
            throw new InternalException();
        } finally {
            inputStream = null;
        }
    }
}
