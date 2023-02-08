package expression.exceptions;

public class StringSource implements CharSource {
    private final String data;
    private int pos = 0;

    public StringSource(final String data) {
        this.data = data;
    }

    @Override
    public boolean hasNext() {
        return pos < data.length();
    }

    @Override
    public int getPos() {
        return pos;
    }

    @Override
    public void back(int steps) {
        if (pos < steps) {
            throw new AssertionError("pos is less than steps, can not go back");
        }
        pos -= steps;
    }

    @Override
    public char next() {
        return data.charAt(pos++);
    }

    @Override
    public IllegalArgumentException error(final String message) {
        return new IllegalArgumentException(pos + ": " + message);
    }

    @Override
    public String toString() {
        return data;
    }

}
