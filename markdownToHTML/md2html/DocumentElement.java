package md2html;

import java.util.List;

public class DocumentElement extends HtmlElement {

    public DocumentElement(String htmlTag, List<HtmlElement> list){
        super(htmlTag, List.copyOf(list));
    }

    @Override
    public void toHtml(StringBuilder stringBuilder) {
        super.toHtml(stringBuilder);
        stringBuilder.append(System.lineSeparator());
    }

}
