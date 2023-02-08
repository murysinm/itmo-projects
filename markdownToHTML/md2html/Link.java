package md2html;

import java.util.List;

public class Link extends HtmlElement {

    String href;

    public Link(String href, HtmlElement htmlElement){
        super("", List.of(htmlElement));
        this.href = href;
    }

    @Override
    public void toHtml(StringBuilder stringBuilder) {
        stringBuilder.append("<a href='");
        stringBuilder.append(href).append("'>");
        stringBuilder.append(content);
        stringBuilder.append("</a>");
    }

}
