package expression.exceptions;

public class WrongLogArgumentException extends ExpressionEvaluatingException {
    public WrongLogArgumentException(String message) {
        super(message);
    }

    public WrongLogArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
