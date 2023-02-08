package expression.exceptions;

public class ExpressionDBZException extends ExpressionEvaluatingException {
    public ExpressionDBZException(String message) {
        super(message);
    }

    public ExpressionDBZException(String message, Throwable cause) {
        super(message, cause);
    }
}
