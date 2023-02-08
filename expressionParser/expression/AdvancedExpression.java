package expression;

public interface AdvancedExpression extends Expression, DoubleExpression, TripleExpression {

    int getPriority();

    boolean isLeftExtendable(); // b * (a) == b * a, '*' имеет приоритет, равный приоритету выражения а
                                    // false, если в выражении нет операции

    boolean isRightExtendable(); // (a) * b == a * b, '*' имеет приоритет, равный приоритету выражения а
                                    // false, если в выражении нет операции

}
