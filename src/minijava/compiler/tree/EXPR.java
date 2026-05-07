package minijava.compiler.tree;

/**
 * Avalia uma expressão e descarta seu resultado.
 */
public class EXPR extends Stm {
    public Exp exp;
    public EXPR(Exp e) { exp = e; }
}