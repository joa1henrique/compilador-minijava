package minijava.compiler.ast;

import minijava.compiler.Token;

/**
 * Representa a classe principal (public static void main).
 */
public class MainClass extends ASTNode {
    public final Token className;
    public final Token argName;
    public final Statement mainStatement;

    public MainClass(Token className, Token argName, Statement mainStatement) {
        this.className = className;
        this.argName = argName;
        this.mainStatement = mainStatement;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitMainClass(this);
    }
}

