package expression.exceptions;

public class ExpressionOverflowException extends ExpressionEvaluatingException {

    public ExpressionOverflowException(String message) {
        super(message);
    }

    public ExpressionOverflowException(String message, Throwable cause) {
        super(message, cause);
    }
}
