package md2html;

import java.util.List;

public class Header extends DocumentElement {

    public Header(List<HtmlElement> list, int headerLevel) {
        super("h" + headerLevel, list);
    }

}
