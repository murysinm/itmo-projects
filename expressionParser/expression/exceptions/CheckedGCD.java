package expression.exceptions;

import expression.GCD;
import expression.AdvancedExpression;

public class CheckedGCD extends GCD {

    public CheckedGCD(AdvancedExpression leftOperand, AdvancedExpression rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public int compute(int a, int b) {
        return checkedGCD(a, b);
    }

    public static int checkedGCD(int a, int b) {
        while (b != 0) {
            a %= b;
            int c = b;
            b = a;
            a = c;
        }
        return abs(a);
    }

    private static int abs(int a) {
        if (a < 0) {
            a *= -1;
        }
        return a;
    }

}
