package expression;

public class Log10 extends UnaryOperation {

    public Log10(AdvancedExpression operand) {
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
        return "log10";
    }

    @Override
    protected int compute(int a) {
        int log10 = 0;
        while (a != 0) {
            a /= 10;
            log10++;
        }
        return log10 - 1;
    }

    @Override
    protected double compute(double x){
        throw new AssertionError("can not compute log10 of double value");
    }

}
