package mg.itu.prom16.view.layout;

import jakarta.servlet.ServletException;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import mg.itu.prom16.utils.JspUtils;
import mg.matsd.javaframework.core.utils.Assert;

import java.io.IOException;

public class ExtendsTag extends SimpleTagSupport {
    private String file;

    public void setFile(String file) {
        Assert.notBlank(file, true, "Le nom de la vue étendre ne peut pas être vide");

        if (!file.endsWith(".jsp")) file += ".jsp";
        this.file = file;
    }

    @Override
    public void doTag() throws JspException, IOException {
        JspUtils.invokeJspFragment(getJspBody());

        try {
            ((PageContext) getJspContext()).forward(file);
        } catch (ServletException e) {
            throw new JspException(e.getMessage(), e);
        }
    }
}
