package minijava.compiler.ast;

/**
 * Representa uma expressão entre parênteses: (expr)
 */
public class ParenthesizedExpression extends Expression {
    public final Expression expression;

    public ParenthesizedExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitParenthesizedExpression(this);
    }
}

