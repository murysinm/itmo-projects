package md2html;

import java.util.List;

public class HtmlText extends HtmlElement {

    HtmlText(List<HtmlElement> list) {
        super("", list);
    }

    @Override
    public void toHtml(StringBuilder stringBuilder) {
        stringBuilder.append(content);
    }

}
