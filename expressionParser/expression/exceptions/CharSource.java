package expression.exceptions;

public interface CharSource {
    boolean hasNext();

    char next();

    int getPos();

    void back(int steps);

    IllegalArgumentException error(String message);
}
