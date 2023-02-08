package expression;

public class Subtract extends BinaryOperation {

    public Subtract(AdvancedExpression leftOperand, AdvancedExpression rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public boolean isLeftExtendable() {
        return true;
    }

    @Override
    public boolean isRightExtendable() {
        return false;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public String getSymbol() {
        return "-";
    }

    @Override
    protected double compute(double a, double b) {
        return a - b;
    }

    @Override
    protected int compute(int a, int b) {
        return a - b;
    }
}
