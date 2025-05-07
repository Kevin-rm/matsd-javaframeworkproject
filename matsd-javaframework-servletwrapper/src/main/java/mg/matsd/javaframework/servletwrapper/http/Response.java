package mg.matsd.javaframework.servletwrapper.http;

import jakarta.servlet.http.HttpServletResponse;
import mg.matsd.javaframework.core.utils.Assert;

public class Response {
    protected final HttpServletResponse raw;

    public Response(HttpServletResponse raw) {
        Assert.notNull(raw, "L'argument raw ne peut pas être \"null\"");
        this.raw = raw;
    }

    public HttpServletResponse getRaw() {
        return raw;
    }


}
