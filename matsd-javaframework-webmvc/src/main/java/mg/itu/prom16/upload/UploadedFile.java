package mg.itu.prom16.upload;

import jakarta.servlet.http.Part;
import mg.itu.prom16.utils.WebFacade;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

public class UploadedFile {
    @Nullable
    private String name;
    private byte[] data;
    private final Part part;

    public UploadedFile(Part part) throws FileUploadException {
        Assert.notNull(part, "L'argument \"part\" ne peut pas être \"null\"");
        this.part = part;

        setData();
    }

    @Nullable
    public String getName() {
        return name;
    }

    public UploadedFile setName(@Nullable String name) {
        if (name == null || StringUtils.isBlank(name)) return this;

        this.name = name.replaceAll("^/+|/+$", "");
        return this;
    }

    public byte[] getData() {
        return data;
    }

    private void setData() {
        try (InputStream inputStream = part.getInputStream()) {
            data = inputStream.readAllBytes();
        } catch (IOException e) {
            throw new FileUploadException("Échec de la lecture des données de fichier", e);
        }
    }

    public String getOriginalClientName() {
        return part.getSubmittedFileName();
    }

    public long getSize() {
        return part.getSize();
    }

    public String getContentType() {
        return part.getContentType();
    }

    public void store(String path) throws FileUploadException {
        Assert.notBlank(path, false, "Le répertoire de destination du fichier importé ne peut pas être vide ou \"null\"");

        Path targetPath = Path.of(WebFacade.getCurrentRequest().getServletContext().getRealPath("") +
            File.separator + path);

        try {
            Files.createDirectories(targetPath);
            Files.write(targetPath.resolve((name == null) ? getOriginalClientName() : name),
                data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (FileAlreadyExistsException e) {
            throw new FileUploadException(String.format("Le chemin précisé \"%s\" n'est pas un dossier", path));
        } catch (IOException e) {
            throw new FileUploadException(e);
        }
    }
}
