package expression;

public class LCM extends BinaryOperation {

    public LCM(AdvancedExpression leftOperand, AdvancedExpression rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected double compute(double a, double b) {
        throw new AssertionError("Can't compute LCM of double values");
    }

    @Override
    protected int compute(int a, int b) {
        if (a == 0 && b == 0) {
            return 0;
        }
        long gcd = GCD.getGCD(a, b);
        return (int)((long)a * (long)b / gcd);
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
        return "lcm";
    }

    @Override
    protected boolean checkForRightBrackets() {
        if (getPriority() != rightOperand.getPriority()) {
            return getPriority() > rightOperand.getPriority();
        }
        return !(rightOperand instanceof LCM) && !(isRightExtendable() && rightOperand.isLeftExtendable());
    }

}
