package minijava.compiler.ast;

/**
 * Representa um comando return.
 */
public class ReturnStatement extends Statement {
    public final Expression expression;

    public ReturnStatement(Expression expression) {
        this.expression = expression;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitReturnStatement(this);
    }
}

