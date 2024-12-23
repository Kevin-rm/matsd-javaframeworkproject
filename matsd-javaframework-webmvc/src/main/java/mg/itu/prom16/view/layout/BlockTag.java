package mg.itu.prom16.view.layout;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import mg.itu.prom16.utils.JspUtils;
import mg.matsd.javaframework.core.utils.Assert;

import java.io.IOException;

public class BlockTag extends SimpleTagSupport {
    private String name;

    public void setName(String name) {
        Assert.notBlank(name, true, "Le nom de bloc ne peut pas être vide");
        this.name = name;
    }

    @Override
    public void doTag() throws JspException, IOException {
        PageContext pageContext = (PageContext) getJspContext();

        PutTag.Put put = getPut(pageContext);
        put.type().write(pageContext.getOut(), JspUtils.invokeJspFragment(getJspBody()), put.contents());
    }

    private PutTag.Put getPut(PageContext pageContext) {
        PutTag.Put put = (PutTag.Put) pageContext.findAttribute(PutTag.ATTRIBUTE_KEY_PREFIX + name);

        final PutTag.Put result = put == null ? new PutTag.Put(null, PutTag.DEFAULT_TYPE, "") : put;
        pageContext.removeAttribute(PutTag.ATTRIBUTE_KEY_PREFIX + name, PageContext.PAGE_SCOPE);

        return result;
    }
}
