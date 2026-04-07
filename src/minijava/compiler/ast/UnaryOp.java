package minijava.compiler.ast;

/**
 * Representa uma operação unária.
 */
public class UnaryOp extends Expression {
    public enum Operator {
        NOT
    }

    public final Operator operator;
    public final Expression operand;

    public UnaryOp(Operator operator, Expression operand) {
        this.operator = operator;
        this.operand = operand;
        this.type = new Type(Type.BaseType.BOOLEAN);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitUnaryOp(this);
    }
}

