package expression.exceptions;

import expression.AdvancedExpression;
import expression.Multiply;

public class CheckedMultiply extends Multiply {

    public CheckedMultiply(AdvancedExpression leftOperand, AdvancedExpression rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public int compute(int a, int b) {
        return checkedMultiplication(a, b);
    }

    public static int checkedMultiplication(int a, int b) {
        int result = a * b;
        if (((b != 0) && (result / b != a)) || (a == Integer.MIN_VALUE && b == -1)) {
            throw new ExpressionOverflowException("overflow while multiplying " + a + " by " + b);
        }
        return result;
    }

}
