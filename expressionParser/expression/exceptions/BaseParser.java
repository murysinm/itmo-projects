package expression.exceptions;

public class BaseParser {
    private static final char END = '\0';
    private final CharSource source;
    private char ch = 0xffff;

    protected BaseParser(final CharSource source) {
        this.source = source;
        take();
    }
    protected char take() {
        final char result = ch;
        ch = source.hasNext() ? source.next() : END;
        return result;
    }

    protected int getPos() {
        if (eof()) return source.getPos() + 1;
        return source.getPos();
    }

    protected char get() {
        return ch;
    }

    protected String getE() {
        if (eof()) {
            return "EOF";
        } else {
            return Character.toString(get());
        }
    }

    protected boolean test(final char expected) {
        return ch == expected;
    }

    protected void back(int steps) {
        source.back(steps + 1);
        take();
    }

    protected boolean take(final char expected) {
        if (test(expected)) {
            take();
            return true;
        }
        return false;
    }

    protected void expect(final char expected) {
        if (eof()) {
            throw new ExpressionParsingException("Expected '" + expected + "', but reached end of input");
        }
        if (!take(expected)) {
            throw new ExpressionParsingException("Expected '" + expected + "', found '" + ch + "' at pos " + getPos());
        }
    }

    protected void expect(final String value) {
        for (final char c : value.toCharArray()) {
            expect(c);
        }
    }

    protected boolean eof() {
        return take(END);
    }

    protected IllegalArgumentException error(final String message) {
        return source.error(message);
    }

    protected boolean between(final char from, final char to) {
        return from <= ch && ch <= to;
    }
}
