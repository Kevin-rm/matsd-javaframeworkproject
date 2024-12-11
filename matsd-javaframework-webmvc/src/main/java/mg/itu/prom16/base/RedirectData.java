package mg.itu.prom16.base;

import jakarta.servlet.http.HttpServletRequest;
import mg.itu.prom16.base.internal.UtilFunctions;
import mg.itu.prom16.utils.WebFacade;
import mg.itu.prom16.utils.WebUtils;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

import java.util.*;

public class RedirectData {
    public static final String MANAGED_INSTANCE_ID = "_matsd_redirect_data";
    static final String KEY_PREFIX = "redirect_data.";

    private final Map<String, List<String>> redirectRequestParameters = new HashMap<>();

    public RedirectData add(String key, @Nullable Object value) {
        WebFacade.getFlashBag().set(KEY_PREFIX + key, value);
        return this;
    }

    public RedirectData addAll(Map<String, ?> map) {
        Assert.notNull(map, "L'argument map ne peut pas être \"null\"");

        map.forEach(this::add);
        return this;
    }

    public RedirectData addParameter(String key, @Nullable String value) {
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

    public RedirectData addParameter(String key, @Nullable String[] value) {
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

    public RedirectData appendCurrentQueryString(HttpServletRequest request) {
        Assert.notNull(request, "L'argument request ne peut pas être \"null\"");

        Map<String, String[]> parameterMap = request.getParameterMap();
        parameterMap.forEach(this::addParameter);

        return this;
    }

    String buildCompleteUrl(final String url) {
        StringBuilder stringBuilder;
        if (UtilFunctions.isAbsoluteUrl(url))
             stringBuilder = new StringBuilder(url);
        else stringBuilder = new StringBuilder(WebUtils.absolutePath(url));
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
