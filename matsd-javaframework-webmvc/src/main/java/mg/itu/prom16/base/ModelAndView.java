package mg.itu.prom16.base;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    private String view;
    private final Map<String, Object> data;

    public ModelAndView(String view) {
        setView(view);
        data = new HashMap<>();
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        Assert.notBlank(view, false, "La vue ne peut pas être vide ou \"null\"");

        view = view.strip().replaceAll("^/+", "/");
        if (!view.startsWith("/"))
            view = "/" + view;

        if (!view.endsWith(".jsp"))
            view += ".jsp";

        this.view = view;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public ModelAndView addData(String key, @Nullable Object value) {
        Assert.notBlank(key, false, "La clé de la donnée à ajouter ne peut pas être vide ou \"null\"");
        Assert.state(!(value instanceof ModelAndView), "La donnée ne peut pas être une instance de \"ModelView\"");

        data.put(key, value);
        return this;
    }

    @Nullable
    public Object getData(String key) {
        Assert.notBlank(key, false, "La clé de la donnée à récupérer ne peut pas être vide ou \"null\"");

        if (!data.containsKey(key)) return null;
        return data.get(key);
    }
}
