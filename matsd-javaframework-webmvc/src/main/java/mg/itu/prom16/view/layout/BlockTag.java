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
        getPutType(pageContext).write(pageContext.getOut(), JspUtils.invokeJspFragment(getJspBody()), getPutContents(pageContext));
    }

    private PutType getPutType(PageContext pageContext) {
        PutType putType = (PutType) pageContext.findAttribute(JspUtils.blockTypeAttributeName(name));
        return putType == null ? PutTag.DEFAULT_TYPE : putType;
    }

    private String getPutContents(PageContext pageContext) {
        String putContents = (String) pageContext.findAttribute(JspUtils.blockContentsAttributeName(name));
        return putContents == null ? "" : putContents;
    }
}
