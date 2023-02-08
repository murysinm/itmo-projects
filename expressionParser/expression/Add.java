package expression;

public class Add extends BinaryOperation {

    public Add(AdvancedExpression leftOperand, AdvancedExpression rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected double compute(double a, double b) {
        return a + b;
    }

    @Override
    protected int compute(int a, int b) {
        return a + b;
    }

    @Override
    public boolean isLeftExtendable() {
        return true;
    }

    @Override
    public boolean isRightExtendable() {
        return true;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public String getSymbol() {
        return "+";
    }
}
