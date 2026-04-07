package minijava.compiler.ast;

/**
 * Classe base para representar expressões.
 */
public abstract class Expression extends ASTNode {
    public Type type;  // Preenchido durante análise semântica
}

