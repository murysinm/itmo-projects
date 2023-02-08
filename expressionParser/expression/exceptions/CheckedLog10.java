package expression.exceptions;

import expression.AdvancedExpression;
import expression.Log10;

public class CheckedLog10 extends Log10 {

    public CheckedLog10(AdvancedExpression expression) {
        super(expression);
    }

    @Override
    public int compute(int a) {
        return checkedLog10(a);
    }

    public static int checkedLog10(int a) {
        if (a <= 0) {
            throw new WrongLogArgumentException("can not calculate logarithm of non-positive number " + a);
        }
        int log10 = 0;
        while (a > 0) {
            log10++;
            a /= 10;
        }
        return log10 - 1;

    }

}
