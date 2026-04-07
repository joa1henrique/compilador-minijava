package minijava.compiler.ast;

/**
 * Representa System.out.println(expr);
 */
public class PrintStatement extends Statement {
    public final Expression expression;

    public PrintStatement(Expression expression) {
        this.expression = expression;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitPrintStatement(this);
    }
}

