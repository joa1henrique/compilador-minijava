package minijava.compiler.ast;

/**
 * Representa um comando while.
 */
public class WhileStatement extends Statement {
    public final Expression condition;
    public final Statement body;

    public WhileStatement(Expression condition, Statement body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitWhileStatement(this);
    }
}

