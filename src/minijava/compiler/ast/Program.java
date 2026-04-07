package minijava.compiler.ast;

import java.util.List;

/**
 * Representa o nó raiz da AST.
 * Um programa MiniJava consiste de uma classe principal e classes auxiliares.
 */
public class Program extends ASTNode {
    public final MainClass mainClass;
    public final List<ClassDecl> classes;

    public Program(MainClass mainClass, List<ClassDecl> classes) {
        this.mainClass = mainClass;
        this.classes = classes;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitProgram(this);
    }
}

