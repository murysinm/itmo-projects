package expression.exceptions;

import expression.Negate;
import expression.AdvancedExpression;

public class CheckedNegate extends Negate {

    public CheckedNegate(AdvancedExpression expression) {
        super(expression);
    }

    @Override
    public int compute(int a) {
        return checkedNegation(a);
    }

    public static int checkedNegation(int a) {
        if (a == Integer.MIN_VALUE) {
            throw new ExpressionOverflowException("overflow while negating " + a);
        }
        return -1 * a;
    }

}
