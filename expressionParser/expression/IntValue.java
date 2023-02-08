package expression;

import java.util.Objects;

public class IntValue implements Value {

    private final int value;

    public IntValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntValue intValue = (IntValue) o;
        return getValue() == intValue.getValue();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }
}
