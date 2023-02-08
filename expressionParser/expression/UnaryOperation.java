package expression;

public abstract class UnaryOperation implements AdvancedExpression {
    protected final AdvancedExpression operand;

    protected UnaryOperation(AdvancedExpression operand) {
        this.operand = operand;
    }

    @Override
    public abstract boolean isLeftExtendable();

    @Override
    public abstract boolean isRightExtendable();

    public abstract int getPriority();

    public abstract String getSymbol();

    protected abstract double compute(double a);

    protected abstract int compute(int a);


    @Override
    public int evaluate(int x) {
        return compute(operand.evaluate(x));
    }

    @Override
    public double evaluate(double x) {
        return compute(operand.evaluate(x));
    }

    @Override
    public int evaluate(int x, int y, int z) {
        return compute(operand.evaluate(x, y, z));
    }


    @Override
    public String toString() {
        return getSymbol() + "(" + operand + ")";
    }

    @Override
    public String toMiniString() {
        if (operand.getPriority() == Integer.MAX_VALUE) {
            return getSymbol() + " " + operand.toMiniString();
        } else {
            return getSymbol()  + "(" + operand.toMiniString() + ")";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnaryOperation that = (UnaryOperation) o;
        return operand.equals(that.operand);
    }


    @Override
    public int hashCode() {
        return getClass().hashCode() * 47 + operand.hashCode();
    }
}
