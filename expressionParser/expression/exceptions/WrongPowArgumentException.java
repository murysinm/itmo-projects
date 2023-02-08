package expression.exceptions;

public class WrongPowArgumentException extends ExpressionEvaluatingException {
    public WrongPowArgumentException(String message) {
        super(message);
    }

    public WrongPowArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
