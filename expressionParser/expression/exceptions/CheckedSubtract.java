package expression.exceptions;

import expression.AdvancedExpression;
import expression.Subtract;

public class CheckedSubtract extends Subtract {

    public CheckedSubtract(AdvancedExpression leftOperand, AdvancedExpression rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public int compute(int a, int b) {
        return checkedSubtraction(a, b);
    }

    public static int checkedSubtraction(int a, int b) {
        int result = a - b;
        if (((a ^ b) & (a ^ result)) < 0) {
            throw new ExpressionOverflowException("overflow while subtracting " + a + " and " + b);
        }
        return result;
    }

}
