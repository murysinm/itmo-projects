package expression.exceptions;

import expression.AdvancedExpression;
import expression.LCM;

import static expression.exceptions.CheckedDivide.checkedDivision;
import static expression.exceptions.CheckedMultiply.checkedMultiplication;
import static expression.exceptions.CheckedGCD.checkedGCD;

public class CheckedLCM extends LCM {

    public CheckedLCM(AdvancedExpression leftOperand, AdvancedExpression rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public int compute(int a, int b) {
        return checkedLCM(a, b);
    }

    public static int checkedLCM(int a, int b) {
        if (a == 0 && b == 0) {
            return 0;
        }
        try {
            return checkedMultiplication(checkedDivision(a, checkedGCD(a, b)), b);
        } catch (ExpressionEvaluatingException e) {
            throw new ExpressionEvaluatingException("exception occurred while calculating LCM of " + a + " and " + b, e);
        }
    }

}
