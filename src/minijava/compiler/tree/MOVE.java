package minijava.compiler.tree;

public class MOVE extends Stm {
    public Exp dst, src;
    public MOVE(Exp d, Exp s) { dst = d; src = s; }
}