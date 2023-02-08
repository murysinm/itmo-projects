package expression;

public class Reverse extends UnaryOperation {

    public Reverse(AdvancedExpression operand) {
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
        return "reverse";
    }

    @Override
    protected int compute(int x) {
        long copy = x;
        int sign = (int)Math.signum(copy);
        copy *= sign;
        return sign * (int)Long.parseLong((new StringBuilder(Long.toString(copy)).reverse()).toString());
    }

    @Override
    protected double compute(double x){
        throw new AssertionError("reverse for double values isn't allowed");
    }

}
