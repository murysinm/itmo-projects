package expression.exceptions;

import expression.AdvancedExpression;
import expression.Pow10;

public class CheckedPow10 extends Pow10 {

    public CheckedPow10(AdvancedExpression expression) {
        super(expression);
    }

    @Override
    public int compute(int a) {
        return checkedPow10(a);
    }

    public static int checkedPow10(int a) {
        if (a < 0) {
            throw new WrongPowArgumentException("can not calculate pow10 of negative number " + a);
        }
        if (a > 9) {
            throw new ExpressionOverflowException("overflow occurred while calculating 10 to the power of " + a);
        }
        int result = 1;
        while (a > 0) {
            result *= 10;
            a--;
        }
        return result;
    }
}
