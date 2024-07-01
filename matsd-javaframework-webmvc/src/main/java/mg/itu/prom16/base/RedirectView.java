package mg.itu.prom16.base;

import jakarta.servlet.http.HttpServletRequest;
import mg.itu.prom16.base.internal.UtilFunctions;
import mg.itu.prom16.utils.WebUtils;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

import java.util.*;

public class RedirectView {
    private String url;
    private final Map<String, List<String>> redirectRequestParameters;

    public RedirectView(String url) {
        setUrl(url);
        redirectRequestParameters = new HashMap<>();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        Assert.notBlank(url, false, "L'url ne peut pas être vide ou \"null\"");

        this.url = url.strip();
    }

    public RedirectView withParameter(String key, @Nullable String value) {
        Assert.notBlank(key, false, "La clé de paramètre ne peut pas être vide ou \"null\"");
        if (value == null) return this;

        if (!redirectRequestParameters.containsKey(key))
            redirectRequestParameters.put(
                key, new ArrayList<>(List.of(value))
            );
        else {
            List<String> redirectRequestParametersValues = redirectRequestParameters.get(key);
            redirectRequestParametersValues.add(value);
        }

        return this;
    }

    public RedirectView withParameter(String key, @Nullable String[] value) {
        Assert.notBlank(key, false, "La clé de paramètre ne peut pas être vide ou \"null\"");
        if (value == null || value.length == 0) return this;

        if (!redirectRequestParameters.containsKey(key))
            redirectRequestParameters.put(
                key, new ArrayList<>(Arrays.asList(value))
            );
        else {
            List<String> redirectRequestParametersValues = redirectRequestParameters.get(key);
            redirectRequestParametersValues.addAll(Arrays.asList(value));
        }

        return this;
    }

    public RedirectView appendCurrentQueryString(HttpServletRequest request) {
         Assert.notNull(request, "L'argument request ne peut pas être \"null\"");

         Map<String, String[]> parameterMap = request.getParameterMap();
         parameterMap.forEach(this::withParameter);

         return this;
    }


    String buildCompleteUrl(HttpServletRequest httpServletRequest) {
        if (UtilFunctions.isAbsoluteUrl(url)) return url;

        StringBuilder stringBuilder = new StringBuilder(WebUtils.absolutePath(httpServletRequest, url));
        if (redirectRequestParameters.isEmpty()) return stringBuilder.toString();

        stringBuilder.append("?");
        redirectRequestParameters.forEach((key, values) ->
            values.forEach(value -> stringBuilder.append(key)
                .append("=")
                .append(value)
                .append("&")
            )
        );
        stringBuilder.setLength(stringBuilder.length() - 1);

        return stringBuilder.toString();
    }
}
