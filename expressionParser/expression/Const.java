package expression;

import java.util.Objects;

public class Const implements AdvancedExpression {

    private final Value value;

    public Const(int value) {
        this.value = new IntValue(value);
    }

    public Const(double value) {
        this.value = new DoubleValue(value);
    }

    @Override
    public String toMiniString() {
        return value.toString();
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
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
    public double evaluate(double x) {
        return value.getValue();
    }

    @Override
    public int evaluate(int x) {
        return (int) value.getValue();
    }

    @Override
    public int evaluate(int x, int y, int z) {
        return (int) value.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Const aConst = (Const) o;
        return Objects.equals(value, aConst.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
