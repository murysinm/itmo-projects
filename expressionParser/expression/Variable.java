package expression;

import java.util.Objects;

public class Variable implements AdvancedExpression {
    // :NOTE: access
    String name;

    public Variable(String name) {
        switch (name) {
            case "x", "y", "z" -> this.name = name;
            default -> throw new UnsupportedOperationException("Unsupported variable name: \"" + name + "\"");
        };
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toMiniString() {
        return name;
    }

    @Override
    public String toString() {
        return name;
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
        if (!name.equals("x")) {
            throw new UnsupportedOperationException("Too few arguments: given 1, need more");
        }
        return x;
    }

    @Override
    public int evaluate(int x) {
        if (!name.equals("x")) {
            throw new UnsupportedOperationException("Too few arguments: given 1, need more");
        }
        return x;
    }

    @Override
    public int evaluate(int x, int y, int z) {
        return switch (name) {
            case "x" -> x;
            case "y" -> y;
            case "z" -> z;
            default -> throw new AssertionError("Unsupported variable name");
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return Objects.equals(name, variable.name);
    }

}
