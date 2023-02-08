package expression.exceptions;

import expression.AdvancedExpression;
import expression.Divide;

public class CheckedDivide extends Divide {

    public CheckedDivide(AdvancedExpression leftOperand, AdvancedExpression rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public int compute(int a, int b) {
        return checkedDivision(a, b);
    }

    public static int checkedDivision(int a, int b) {
        if (a == Integer.MIN_VALUE && b == -1) {
            throw new ExpressionOverflowException("overflow while dividing " + a + " by " + b);
        }
        if (b == 0) {
            throw new ExpressionDBZException("division by zero");
        }
        return a / b;
    }

}
