package expression.exceptions;

import expression.TripleExpression;

public class Main {
    public static void main(String[] args) {
        final ExpressionParser parser = new ExpressionParser();
        TripleExpression expr = parser.parse("1000000*x*x*x*x*x/(x-1)");
        for (int i = 0; i <= 10; i++) {
            System.out.print("x = " + i + ": ");
            try {
                System.out.println("f = " + expr.evaluate(i, 0, 0));
            } catch (ExpressionEvaluatingException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
