package expression.exceptions;

import expression.Reverse;
import expression.AdvancedExpression;

public class CheckedReverse extends Reverse {

    public CheckedReverse(AdvancedExpression expression) {
        super(expression);
    }

    @Override
    public int compute(int a) {
        return checkedReverse(a);
    }

    public static int checkedReverse(int a) {
        if (a == Integer.MIN_VALUE) {
            throw new ExpressionOverflowException("can't reverse " + a);
        }
        int sign = 1;
        if (a < 0) {
            sign = -1;
        }
        a *= sign;
        try {
            return sign * Integer.parseInt((new StringBuilder(Integer.toString(a)).reverse()).toString());
        } catch (NumberFormatException e) {
            throw new ExpressionOverflowException("can't reverse " + a, e);
        }
    }
}
