package minijava.compiler.tree;

public class MEM extends Exp {
    public Exp exp;
    public MEM(Exp e) { exp = e; }
}