package minijava.compiler.ast;

import minijava.compiler.Token;

/**
 * Representa uma atribuição em array: id[expr] = expr;
 */
public class ArrayAssignmentStatement extends Statement {
    public final Token varName;
    public final Expression index;
    public final Expression value;

    public ArrayAssignmentStatement(Token varName, Expression index, Expression value) {
        this.varName = varName;
        this.index = index;
        this.value = value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitArrayAssignmentStatement(this);
    }
}

