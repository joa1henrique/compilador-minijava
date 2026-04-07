package minijava.compiler.ast;

/**
 * Classe base para todos os nós da Árvore Sintática Abstrata (AST).
 */
public abstract class ASTNode {
    public abstract <T> T accept(Visitor<T> visitor);
}

