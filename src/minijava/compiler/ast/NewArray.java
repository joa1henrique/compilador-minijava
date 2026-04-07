package minijava.compiler.ast;

/**
 * Representa a criação de um novo array: new int[expr]
 */
public class NewArray extends Expression {
    public final Expression sizeExpression;

    public NewArray(Expression sizeExpression) {
        this.sizeExpression = sizeExpression;
        this.type = new Type(Type.BaseType.INT, true);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitNewArray(this);
    }
}

