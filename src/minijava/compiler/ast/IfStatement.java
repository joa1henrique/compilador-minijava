package minijava.compiler.ast;

/**
 * Representa um comando if.
 */
public class IfStatement extends Statement {
    public final Expression condition;
    public final Statement thenStatement;
    public final Statement elseStatement;  // null se sem else

    public IfStatement(Expression condition, Statement thenStatement, Statement elseStatement) {
        this.condition = condition;
        this.thenStatement = thenStatement;
        this.elseStatement = elseStatement;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitIfStatement(this);
    }
}

