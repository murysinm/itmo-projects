package md2html;

import java.util.ArrayList;
import java.util.List;

public class PlainText extends HtmlElement {

    public PlainText(String content) {
        super("", new StringBuilder(content));
    }

    @Override
    public void toHtml(StringBuilder stringBuilder) {
        int pos = 0;
        while (pos < content.length()) {
            if (content.charAt(pos) == '\\' && pos + 1 < content.length()) {
                stringBuilder.append(content.charAt(pos + 1));
                pos += 2;
                continue;
            }
            switch (content.charAt(pos)) {
                case '<' -> stringBuilder.append("&lt;");
                case '>' -> stringBuilder.append("&gt;");
                case '&' -> stringBuilder.append("&amp;");
                default -> stringBuilder.append(content.charAt(pos));
            };
            pos++;
        }
    }

}
