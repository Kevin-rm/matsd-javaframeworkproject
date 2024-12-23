package mg.itu.prom16.view.layout;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import mg.matsd.javaframework.core.utils.Assert;

import java.io.IOException;

import static mg.itu.prom16.utils.JspUtils.*;

public class PutTag extends SimpleTagSupport {
    public static final PutType DEFAULT_TYPE    = PutType.APPEND;
    public static final String  DATA_KEY_PREFIX = PutTag.class.getCanonicalName() + ".";

    private String block;
    private String type;

    public void setBlock(String block) {
        Assert.notBlank(block, true, "Le bloc ne peut pas être vide");
        this.block = block;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void doTag() throws JspException, IOException {
        if (findAncestorWithClass(this, ExtendsTag.class) == null)
            throw new JspException("Les tags \"put\" doivent être utilisés à l'intérieur d'un tag \"extends\"");

        PageContext pageContext = (PageContext) getJspContext();
        pageContext.setAttribute(blockContentsAttributeName(block), invokeJspFragment(getJspBody()), PageContext.REQUEST_SCOPE);
        pageContext.setAttribute(blockTypeAttributeName(block),
            type == null || type.isBlank() ? DEFAULT_TYPE : PutType.fromString(type),
            PageContext.REQUEST_SCOPE);
    }
}
