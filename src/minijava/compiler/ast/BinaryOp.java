package minijava.compiler.ast;

import minijava.compiler.TokenType;

/**
 * Representa uma operação binária.
 */
public class BinaryOp extends Expression {
    public enum Operator {
        AND, LESS_THAN, PLUS, MINUS, TIMES
    }

    public final Expression left;
    public final Operator operator;
    public final Expression right;

    public BinaryOp(Expression left, Operator operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
        // O tipo será determinado durante a análise semântica
    }

    public static Operator fromTokenType(TokenType tokenType) {
        switch (tokenType) {
            case AND:
                return Operator.AND;
            case LESS_THAN:
                return Operator.LESS_THAN;
            case PLUS:
                return Operator.PLUS;
            case MINUS:
                return Operator.MINUS;
            case TIMES:
                return Operator.TIMES;
            default:
                throw new IllegalArgumentException("Unknown operator: " + tokenType);
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitBinaryOp(this);
    }
}

