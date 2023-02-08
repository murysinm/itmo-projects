package md2html;

import java.util.List;

public class Paragraph extends DocumentElement {

    public Paragraph(List<HtmlElement> list) {
        super("p", list);
    }

}