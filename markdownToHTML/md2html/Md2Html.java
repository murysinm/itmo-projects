package md2html;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Md2Html {
    public static void main(String[] args) {
        final String inputFile = args[0];
        final String outputFile = args[1];
        final String content;
        try {
            content = Files.readString(Paths.get(inputFile), StandardCharsets.UTF_8);
        } catch (final FileNotFoundException e) {
            System.out.println("Input file not found: " + e.getMessage());
            return;
        } catch (final OutOfMemoryError e) {
            System.out.println("Out of memory - input file is too big: " + e.getMessage());
            return;
        } catch (final IOException e) {
            System.out.println("Input error: " + e.getMessage());
            return;
        }
        final String[] splittedContent = content.split(System.lineSeparator() + System.lineSeparator());
        final List<String> documentElements = new ArrayList<>();
        for (String documentElement : splittedContent) {
            documentElement = trimLineSeparators(documentElement);
            if (!documentElement.isEmpty() && !allCharactersAreWhitespaces(documentElement)) {
                documentElements.add(documentElement);
            }
        }
        final List<DocumentElement> HtmlDocumentElements = new ArrayList<>();
        for (final String string : documentElements) {
            HtmlDocumentElements.add(parseDocumentElement(string));
        }
        final HtmlText document = new HtmlText(List.copyOf(HtmlDocumentElements));
        final StringBuilder result = new StringBuilder();
        document.toHtml(result);
        try {
            Files.writeString(Paths.get(outputFile), result, StandardCharsets.UTF_8);
        } catch (final SecurityException e) {
            System.out.println("Access denied: " + e.getMessage());
        } catch (final IOException e) {
            System.out.println("Output error: " + e.getMessage());
        }
    }

    public static String trimLineSeparators(final String s) {
        int start = 0;
        while (start + System.lineSeparator().length() <= s.length()
                && s.startsWith(System.lineSeparator(), start)
        ) {
            start += System.lineSeparator().length();
        }
        int end = s.length();
        while (end - System.lineSeparator().length() >= start
                && s.startsWith(System.lineSeparator(), end - System.lineSeparator().length())
        ) {
            end -= System.lineSeparator().length();
        }
        return s.substring(start, end);
    }

    public static boolean allCharactersAreWhitespaces(final String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) return false;
        }
        return true;
    }

    private static final String NO_TAG = "noTag";

    public static DocumentElement parseDocumentElement(final String content) {
        int currentPos = 0;
        DocumentElementType type = DocumentElementType.PARAGRAPH;
        int headerLevel = 0;
        if (content.charAt(0) == '#') {
            while (currentPos < content.length() && content.charAt(currentPos) == '#') {
                headerLevel++;
                currentPos++;
            }
            if (headerLevel <= 6 &&
                    (currentPos == content.length() || Character.isWhitespace(content.charAt(currentPos)))) {
                currentPos++;
                type = DocumentElementType.HEADER;
            } else {
                currentPos = 0;
            }
        }
        final List<HtmlElement> list = new ArrayList<>();
        parseTag(content, list, currentPos, NO_TAG);
        if (type == DocumentElementType.PARAGRAPH) {
            return new Paragraph(list);
        } else {
            return new Header(list, headerLevel);
        }
    }

    public static String getHtmlTag(String markdownTag) {
        return switch (markdownTag) {
            case "**", "__" -> "strong";
            case "*", "_" -> "em";
            case "`" -> "code";
            case "--" -> "s";
            default -> "";
        };
    }

    public static boolean isEscapable(char c) {
        return c == '\\' || c == '*' || c == '_' || c == '`' || c == '-' || c == '[' || c == ']';
    }

    public static int parseTag (
            final String content, final List<HtmlElement> currentList,
            final int startPos, final String markdownTag
    ) {
        int pos = startPos;
        int lastUnmarkedPos = startPos;
        final List<HtmlElement> newElements = new ArrayList<>();
        final String[] tags = new String[]{"**", "__", "--", "*", "_", "`", "[", "]"};
        outer: while (pos < content.length()) {
            if (content.charAt(pos) == '\\') {
                if (pos == content.length() - 1 || !isEscapable(content.charAt(pos + 1))) {
                    continue;
                }
                pos += 2;
            }
            for (final String tag : tags) {
                if (pos + tag.length() > content.length()) continue;
                if (!tag.equals(content.substring(pos, pos + tag.length()))) continue;
                newElements.add(new PlainText(content.substring(lastUnmarkedPos, pos)));
                if (tag.equals("]")) {
                    if (!markdownTag.equals("]")) continue;
                    if (pos + 1 >= content.length() || content.charAt(pos + 1) != '(') continue;
                    currentList.add(new HtmlText(newElements));
                    return pos + 1;
                }
                if (tag.equals(markdownTag)) {
                    currentList.add(new HtmlElement(getHtmlTag(markdownTag), newElements));
                    return pos + tag.length();
                } else {
                    final List<HtmlElement> newList = new ArrayList<>();
                    if (tag.equals("[")) {
                        pos = parseLink(content, newList, pos + 1);
                    } else {
                        pos = parseTag(content, newList, pos + tag.length(), tag);
                    }
                    lastUnmarkedPos = pos;
                    newElements.addAll(newList);
                    continue outer;
                }
            }
            pos++;
        }
        if (markdownTag.equals(NO_TAG)) {
            newElements.add(new PlainText(content.substring(lastUnmarkedPos)));
            currentList.addAll(newElements);
            return content.length();
        } else {
            currentList.add(new PlainText(markdownTag));
            return startPos;
        }
    }

    public static int parseLink(final String content, final List<HtmlElement> linkContainer, final int startPos){
        int pos = startPos;
        HtmlElement sign = null;
        while (pos < content.length()) {
            if (pos < content.length() - 1 && content.charAt(pos) == ']' && content.charAt(pos + 1) == '(') {
                final List<HtmlElement> signList = new ArrayList<>();
                if (startPos != parseTag(content, signList, startPos, "]")) {
                    sign = new HtmlText(signList);
                    break;
                }
            }
            pos++;
        }
        pos++;
        final int brStPos = pos++;
        String href = null;
        while (pos < content.length()) {
            if (content.charAt(pos) == ')') {
                href = content.substring(brStPos + 1, pos);
                break;
            }
            pos++;
        }
        if (href != null && sign != null) {
            linkContainer.add(new Link(href, sign));
            return ++pos;
        } else {
            linkContainer.add(new PlainText("["));
            return startPos;
        }
    }
}