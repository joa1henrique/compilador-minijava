package minijava.compiler.ast;

import java.util.List;

/**
 * Representa um bloco de comandos { ... }
 */
public class BlockStatement extends Statement {
    public final List<Statement> statements;

    public BlockStatement(List<Statement> statements) {
        this.statements = statements;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitBlockStatement(this);
    }
}

