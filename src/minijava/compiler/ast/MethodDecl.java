package minijava.compiler.ast;

import minijava.compiler.Token;
import java.util.List;

/**
 * Representa a declaração de um método.
 */
public class MethodDecl extends ASTNode {
    public final Type returnType;
    public final Token methodName;
    public final List<VarDecl> parameters;
    public final List<VarDecl> locals;
    public final List<Statement> statements;
    public final Expression returnExpression;

    public MethodDecl(Type returnType, Token methodName, List<VarDecl> parameters,
                      List<VarDecl> locals, List<Statement> statements, Expression returnExpression) {
        this.returnType = returnType;
        this.methodName = methodName;
        this.parameters = parameters;
        this.locals = locals;
        this.statements = statements;
        this.returnExpression = returnExpression;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitMethodDecl(this);
    }
}

