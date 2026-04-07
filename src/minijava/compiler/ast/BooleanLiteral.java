package minijava.compiler.ast;

/**
 * Representa uma literal booleana (true ou false).
 */
public class BooleanLiteral extends Expression {
    public final boolean value;

    public BooleanLiteral(boolean value) {
        this.value = value;
        this.type = new Type(Type.BaseType.BOOLEAN);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitBooleanLiteral(this);
    }
}

