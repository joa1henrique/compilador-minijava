package minijava.compiler.ast;

import minijava.compiler.Token;

/**
 * Representa a palavra-chave 'this'.
 */
public class This extends Expression {
    public final Token token;

    public This(Token token) {
        this.token = token;
        // O tipo será preenchido durante a análise semântica
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitThis(this);
    }
}

