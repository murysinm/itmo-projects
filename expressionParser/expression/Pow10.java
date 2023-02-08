package expression;

public class Pow10 extends UnaryOperation {

    public Pow10(AdvancedExpression operand) {
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
        return "pow10";
    }

    @Override
    protected int compute(int x) {
        int res = 1;
        while(x != 0) {
            res *= 10;
            x--;
        }
        return res;
    }

    @Override
    protected double compute(double x){
        throw new AssertionError("pow10 for double values isn't allowed");
    }

}
