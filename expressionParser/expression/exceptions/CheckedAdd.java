package expression.exceptions;
import expression.*;

public class CheckedAdd extends Add {

    public CheckedAdd(AdvancedExpression leftOperand, AdvancedExpression rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public int compute(int a, int b) {
        return checkedAddition(a, b);
    }

    public static int checkedAddition(int a, int b) {
        int result = a + b;
        if (((a ^ result) & (b ^ result)) < 0) {
            throw new ExpressionOverflowException("overflow while adding " + a + " and " + b);
        }
        return result;
    }

}
