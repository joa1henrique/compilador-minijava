package minijava.compiler.ast;

/**
 * Representa acesso a um elemento de array: array[index]
 */
public class ArrayAccess extends Expression {
    public final Expression array;
    public final Expression index;

    public ArrayAccess(Expression array, Expression index) {
        this.array = array;
        this.index = index;
        // O tipo será preenchido durante a análise semântica
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitArrayAccess(this);
    }
}

