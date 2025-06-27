package mg.itu.prom16.view.layout;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import mg.itu.prom16.utils.JspUtils;
import mg.matsd.javaframework.core.annotations.metadata.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

import java.io.IOException;

public class PutTag extends SimpleTagSupport {
    public static final PutType DEFAULT_TYPE         = PutType.APPEND;
    public static final String  ATTRIBUTE_KEY_PREFIX = PutTag.class.getCanonicalName() + ".";

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
            throw new JspException("Les balises \"put\" doivent être utilisées à l'intérieur d'une balise \"extends\"");

        getJspContext().setAttribute(ATTRIBUTE_KEY_PREFIX + block,
            new Put(block, type == null || type.isBlank() ? DEFAULT_TYPE : PutType.fromString(type),
                JspUtils.invokeJspFragment(getJspBody())), PageContext.REQUEST_SCOPE);
    }

    record Put(@Nullable String blockName, PutType type, String contents) { }
}
