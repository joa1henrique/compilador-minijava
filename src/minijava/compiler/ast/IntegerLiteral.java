package minijava.compiler.ast;

/**
 * Representa uma literal inteira.
 */
public class IntegerLiteral extends Expression {
    public final int value;

    public IntegerLiteral(int value) {
        this.value = value;
        this.type = new Type(Type.BaseType.INT);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitIntegerLiteral(this);
    }
}

