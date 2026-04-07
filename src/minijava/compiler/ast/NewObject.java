package minijava.compiler.ast;

import minijava.compiler.Token;

/**
 * Representa a criação de um novo objeto: new ClassName()
 */
public class NewObject extends Expression {
    public final Token className;

    public NewObject(Token className) {
        this.className = className;
        // O tipo será preenchido durante a análise semântica
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitNewObject(this);
    }
}

