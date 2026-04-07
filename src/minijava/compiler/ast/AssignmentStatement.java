package minijava.compiler.ast;

import minijava.compiler.Token;

/**
 * Representa uma atribuição simples: id = expr;
 */
public class AssignmentStatement extends Statement {
    public final Token varName;
    public final Expression value;

    public AssignmentStatement(Token varName, Expression value) {
        this.varName = varName;
        this.value = value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitAssignmentStatement(this);
    }
}

