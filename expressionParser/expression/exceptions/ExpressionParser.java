package expression.exceptions;

import expression.*;

import java.util.Map;


public final class ExpressionParser implements TripleParser {

    public AdvancedExpression parse(final String source) {
        return parse(new StringSource(source));
    }

    public AdvancedExpression parse(final CharSource source) {
        return new ExprParser(source).startParsing();
    }

    private static class ExprParser extends BaseParser {

        public ExprParser(final CharSource source) {
            super(source);
        }
        public AdvancedExpression startParsing() {
            final AdvancedExpression result = parseWithPriority(Integer.MIN_VALUE);
            if (eof()) {
                return result;
            }
            throw new ExpressionParsingException("Incorrect parenthesis: closing bracket unexpected at pos " + getPos());
        }

        private static final Map<String, Integer> operationPriority = Map.of(
                "+", 100,
                "-", 100,
                "*", 200,
                "/", 200,
                "gcd", 50,
                "lcm", 50
        );

        private AdvancedExpression parseWithPriority(int priority) {
            AdvancedExpression start = parseStart();
            while(true) {
                if (test(')') || eof()) {
                    return start;
                }
                String operation = getOperation();
                if (operationPriority.get(operation) > priority) {
                    start = switch (operation) {
                        case "+" -> new CheckedAdd(start, parseWithPriority(operationPriority.get(operation)));
                        case "-" -> new CheckedSubtract(start, parseWithPriority(operationPriority.get(operation)));
                        case "*" -> new CheckedMultiply(start, parseWithPriority(operationPriority.get(operation)));
                        case "/" -> new CheckedDivide(start, parseWithPriority(operationPriority.get(operation)));
                        case "gcd" -> new CheckedGCD(start, parseWithPriority(operationPriority.get(operation)));
                        case "lcm" -> new CheckedLCM(start, parseWithPriority(operationPriority.get(operation)));
                        default -> throw new AssertionError("unknown operation");
                    };
                } else {
                    back(operation.length());
                    return start;
                }
            }
        }

        private AdvancedExpression parseStart() {
            skipWhitespace();
            final AdvancedExpression start;
            if (take('(')) {
                start = parseBrackets();
            } else if (Character.isDigit(get())) {
                start = parseConst();
            } else if (take('-')) {
                start = parseNegate();
            } else if (test('x') || test('y') || test('z')) {
                start = new Variable(Character.toString(take()));
            } else if (take('r')) {
                expect("everse");
                checkEnd("reverse");
                start = new CheckedReverse(parseWithPriority(Integer.MAX_VALUE));
            } else if (take('p')) {
                expect("ow10");
                checkEnd("pow10");
                start = new CheckedPow10(parseWithPriority(Integer.MAX_VALUE));
            } else if (take('l')) {
                expect("og10");
                checkEnd("log10");
                start = new CheckedLog10(parseWithPriority(Integer.MAX_VALUE));
            } else {
                throw new ExpressionParsingException("unexpected symbol '" + getE() + "' at pos " + getPos() + ". " +
                        "Start of an operand expected here.");
            }
            skipWhitespace();
            return start;
        }

        private String getOperation() {
            if (test('+') || test('-') || test('/') || test('*')) {
                return Character.toString(take());
            } else if (take('l')) {
                expect("cm");
                checkEnd("lcm");
                return "lcm";
            } else if (take('g')) {
                expect("cd");
                checkEnd("gcd");
                return "gcd";
            } else {
                throw new ExpressionParsingException("Unexpected symbol '" + getE() + "' at pos " + getPos() + ". " +
                                                                            "Operation symbol was expected here.");
            }
        }

        private AdvancedExpression parseBrackets() {
            final AdvancedExpression expression = parseWithPriority(Integer.MIN_VALUE);
            if (!test(')')) {
                throw new ExpressionParsingException("closing parenthesis expected at pos " + getPos() + ", found: " + getE());
            }
            expect(')');
            return expression;
        }

        private AdvancedExpression parseNegate() {
            if (Character.isDigit(get())) {
                back(1);
                return parseConst();
            }
            return new CheckedNegate(parseWithPriority(Integer.MAX_VALUE));
        }

        private AdvancedExpression parseConst() {
            final StringBuilder sb = new StringBuilder();
            int startPos = getPos();
            if (take('-')) {
                sb.append('-');
            }
            takeDigits(sb);
            try {
                return new Const(Integer.parseInt(sb.toString()));
            } catch (NumberFormatException e) {
                throw new ExpressionParsingException("overflow while parsing constant at pos " + startPos, e);
            }
        }

        private void takeDigits(final StringBuilder sb) {
            while(Character.isDigit(get())) {
                sb.append(take());
            }
        }

        private void checkEnd(String s) {
            if (eof() || test('-') || test('(') || Character.isWhitespace(get())) {
                return;
            }
            throw new ExpressionParsingException("unexpected symbol after " + s + ": '" + getE() + "'");
        }

        private void skipWhitespace() {
            while(Character.isWhitespace(get())) {
                take();
            }
        }
    }
}
