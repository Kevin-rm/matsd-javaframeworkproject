package mg.itu.prom16.base;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

import java.util.Map;

public class ModelAndView {
    private String view;
    private final Model model;

    public ModelAndView(String view) {
        setView(view);
        model = new Model();
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

    Model getModel() {
        return model;
    }

    public Map<String, Object> getData() {
        return model.getData();
    }

    public ModelAndView addData(String key, @Nullable Object value) {
        model.addData(key, value);
        return this;
    }

    @Nullable
    public Object getData(String key) {
        return model.getData(key);
    }
}
