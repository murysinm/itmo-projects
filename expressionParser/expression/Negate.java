package expression;

public class Negate extends UnaryOperation {

    public Negate(AdvancedExpression operand) {
        super(operand);
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
        return Integer.MAX_VALUE;
    }

    @Override
    public String getSymbol() {
        return "-";
    }

    @Override
    protected int compute(int x){
        return -1 * x;
    }

    @Override
    protected double compute(double x){
        return -1 * x;
    }

}
