package mg.itu.prom16.base;

import jakarta.servlet.http.HttpServletRequest;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

public class RedirectView {
    private String url;
    private final RedirectData redirectData = new RedirectData();

    public RedirectView() { }

    public RedirectView(String url) {
        setUrl(url);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        Assert.notBlank(url, false, "L'url ne peut pas être vide ou \"null\"");

        this.url = url.strip();
    }

    public RedirectData getRedirectData() {
        return redirectData;
    }

    public RedirectView withParameter(String key, @Nullable String value) {
        redirectData.addParameter(key, value);
        return this;
    }

    public RedirectView withParameter(String key, @Nullable String[] value) {
        redirectData.addParameter(key, value);
        return this;
    }

    public RedirectView appendCurrentQueryString(HttpServletRequest request) {
        redirectData.appendCurrentQueryString(request);
        return this;
    }

    String buildCompleteUrl() {
        return redirectData.buildCompleteUrl(url);
    }
}
