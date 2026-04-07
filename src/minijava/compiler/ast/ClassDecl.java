package minijava.compiler.ast;

import minijava.compiler.Token;
import java.util.List;

/**
 * Representa uma declaração de classe.
 */
public class ClassDecl extends ASTNode {
    public final Token className;
    public final Token superClassName;  // null se não houver herança
    public final List<VarDecl> fields;
    public final List<MethodDecl> methods;

    public ClassDecl(Token className, Token superClassName, List<VarDecl> fields, List<MethodDecl> methods) {
        this.className = className;
        this.superClassName = superClassName;
        this.fields = fields;
        this.methods = methods;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitClassDecl(this);
    }
}

