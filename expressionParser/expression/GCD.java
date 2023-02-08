package expression;

public class GCD extends BinaryOperation {

    public GCD(AdvancedExpression leftOperand, AdvancedExpression rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected double compute(double a, double b) {
        throw new AssertionError("Can't compute GCD of double values");
    }

    @Override
    protected int compute(int a, int b) {
        return getGCD(a, b);
    }

    public static int getGCD(int a, int b) {
        if (a == 0 && b == 0) {
            throw new AssertionError("can't compute gcd of 0 and 0");
        }
        long aCopy = a;
        long bCopy = b;
        aCopy = Math.abs(aCopy);
        bCopy = Math.abs(bCopy);
        while (bCopy != 0) {
            aCopy = aCopy % bCopy;
            long c = bCopy;
            bCopy = aCopy;
            aCopy = c;
        }
        return (int)aCopy;
    }

    @Override
    public boolean isLeftExtendable() {
        return false;
    }

    @Override
    public boolean isRightExtendable() {
        return false;
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public String getSymbol() {
        return "gcd";
    }

    @Override
    protected boolean checkForRightBrackets() {
        if (getPriority() != rightOperand.getPriority()) {
            return getPriority() > rightOperand.getPriority();
        }
        return !(rightOperand instanceof GCD) && !(isRightExtendable() && rightOperand.isLeftExtendable());
    }
}
