package minijava.compiler.ast;

import minijava.compiler.Token;

/**
 * Representa a declaração de uma variável.
 */
public class VarDecl extends ASTNode {
    public final Type type;
    public final Token varName;

    public VarDecl(Type type, Token varName) {
        this.type = type;
        this.varName = varName;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitVarDecl(this);
    }
}

