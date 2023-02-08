package md2html;

import java.util.List;

public class HtmlElement {
    final StringBuilder htmlTag;
    final StringBuilder content;

    public HtmlElement(String htmlTag, StringBuilder content) {
        this.htmlTag = new StringBuilder(htmlTag);
        this.content = new StringBuilder(content);
    }

    public HtmlElement(String htmlTag, List<HtmlElement> list) {
        this.htmlTag = new StringBuilder(htmlTag);
        StringBuilder content = new StringBuilder();
        for (HtmlElement htmlElement : list) {
            htmlElement.toHtml(content);
        }
        this.content = content;
    }

    public void toHtml(StringBuilder stringBuilder) {
        stringBuilder.append('<').append(htmlTag).append('>');
        stringBuilder.append(content);
        stringBuilder.append("</").append(htmlTag).append('>');
    }

}
