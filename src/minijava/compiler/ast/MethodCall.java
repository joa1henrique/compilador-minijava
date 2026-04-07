package minijava.compiler.ast;

import minijava.compiler.Token;
import java.util.List;

/**
 * Representa uma chamada de método: object.methodName(args)
 */
public class MethodCall extends Expression {
    public final Expression object;
    public final Token methodName;
    public final List<Expression> arguments;

    public MethodCall(Expression object, Token methodName, List<Expression> arguments) {
        this.object = object;
        this.methodName = methodName;
        this.arguments = arguments;
        // O tipo será preenchido durante a análise semântica
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitMethodCall(this);
    }
}

