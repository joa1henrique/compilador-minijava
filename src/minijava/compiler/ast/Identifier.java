package minijava.compiler.ast;

import minijava.compiler.Token;

/**
 * Representa um identificador (variável ou classe).
 */
public class Identifier extends Expression {
    public final Token name;

    public Identifier(Token name) {
        this.name = name;
        // O tipo será preenchido durante a análise semântica
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitIdentifier(this);
    }
}

