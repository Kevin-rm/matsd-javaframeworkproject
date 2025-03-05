package mg.itu.prom16.base;

import jakarta.servlet.http.HttpServletRequest;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

import java.util.HashMap;
import java.util.Map;

public class Model {
    public static final String MANAGED_INSTANCE_ID = "_matsd_model";

    private final Map<String, Object> data = new HashMap<>();

    public Map<String, Object> getData() {
        return data;
    }

    public Model addData(String key, @Nullable Object value) {
        Assert.notBlank(key, false, "La clé de la donnée à ajouter ne peut pas être vide ou \"null\"");
        Assert.state(!(value instanceof Model) && !(value instanceof ModelAndView),
            "La donnée ne peut pas être une instance de \"Model\" ou \"ModelAndView\"");

        data.put(key, value);
        return this;
    }

    public Model addData(Map<String, ?> map) {
        Assert.notNull(map, "L'argument map ne peut pas être \"null\"");

        map.forEach(this::addData);
        return this;
    }

    public boolean hasData(String key) {
        Assert.notBlank(key, false, "La clé de la donnée ne peut pas être vide ou \"null\"");

        return data.containsKey(key);
    }

    @Nullable
    public Object getData(String key) {
        Assert.notBlank(key, false, "La clé de la donnée à récupérer ne peut pas être vide ou \"null\"");

        if (!data.containsKey(key)) return null;
        return data.get(key);
    }

    void setAttributes(HttpServletRequest httpServletRequest) {
        if (data.isEmpty()) return;

        data.forEach(httpServletRequest::setAttribute);
        data.clear();
    }
}
