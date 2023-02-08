package expression;

public abstract class BinaryOperation implements AdvancedExpression {
    protected final AdvancedExpression leftOperand;
    protected final AdvancedExpression rightOperand;

    protected BinaryOperation(AdvancedExpression leftOperand, AdvancedExpression rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }

    protected abstract double compute(double a, double b);

    protected abstract int compute(int a, int b);

    @Override
    public int evaluate(int x) {
        return compute(leftOperand.evaluate(x), rightOperand.evaluate(x));
    }

    @Override
    public int evaluate(int x, int y, int z) {
        return compute(leftOperand.evaluate(x, y, z), rightOperand.evaluate(x, y, z));
    }

    @Override
    public double evaluate(double x) {
        return compute(leftOperand.evaluate(x), rightOperand.evaluate(x));
    }


    @Override
    public abstract boolean isLeftExtendable();

    @Override
    public abstract boolean isRightExtendable();

    public abstract int getPriority();

    public abstract String getSymbol();


    @Override
    public String toMiniString() {
        StringBuilder result = new StringBuilder();
        if (checkForLeftBrackets()) {
            result.append('(').append(leftOperand.toMiniString()).append(')');
        } else {
            result.append(leftOperand.toMiniString());
        }
        result.append(" ").append(this.getSymbol()).append(" ");
        if (checkForRightBrackets()) {
            result.append('(').append(rightOperand.toMiniString()).append(')');
        } else {
            result.append(rightOperand.toMiniString());
        }
        return result.toString();
    }

    public String toString() {
        return "(" + leftOperand + " " + getSymbol() + " " + rightOperand + ")";
    }

    protected boolean checkForLeftBrackets() {
        return this.getPriority() > leftOperand.getPriority();
    }

    protected boolean checkForRightBrackets() {
        if (this.getPriority() != rightOperand.getPriority()) {
            return this.getPriority() > rightOperand.getPriority();
        }
        return !(this.isRightExtendable() && rightOperand.isLeftExtendable());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BinaryOperation that = (BinaryOperation) o;
        return leftOperand.equals(that.leftOperand) && rightOperand.equals(that.rightOperand);
    }


    @Override
    public int hashCode() {
        return ((leftOperand.hashCode() * 47 + getClass().hashCode()) * 47 + rightOperand.hashCode()) * 47;
    }
}
