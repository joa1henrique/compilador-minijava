package minijava.compiler.ast;

/**
 * Representa acesso ao atributo 'length' de um array: array.length
 */
public class ArrayLength extends Expression {
    public final Expression array;

    public ArrayLength(Expression array) {
        this.array = array;
        this.type = new Type(Type.BaseType.INT);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitArrayLength(this);
    }
}

