package mg.itu.prom16.support;

import jakarta.servlet.ServletContext;
import mg.matsd.javaframework.core.io.Resource;
import mg.matsd.javaframework.core.io.ResourceNotFoundException;
import mg.matsd.javaframework.core.utils.Assert;

import java.io.InputStream;

public class ServletContextResource extends Resource {
    private ServletContext servletContext;

    public ServletContextResource(ServletContext servletContext, String name) {
        super(name);
        setServletContext(servletContext);
        initializeInputStream();
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    private void setServletContext(ServletContext servletContext) {
        Assert.notNull(servletContext, "L'argument servletContext ne peut pas être \"null\"");

        this.servletContext = servletContext;
    }

    @Override
    protected void initializeInputStream() throws ResourceNotFoundException {
        InputStream inputStream = servletContext.getResourceAsStream(name);
        if (inputStream == null)
            throw new ResourceNotFoundException(
                String.format("Le fichier \"%s\" est introuvable dans le \"servletContext\". " +
                    "Assurez-vous que le chemin d'accès est correct et que la ressource existe", name)
            );

        this.inputStream = inputStream;
    }
}
