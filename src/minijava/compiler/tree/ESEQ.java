package minijava.compiler.tree;

public class ESEQ extends Exp {
    public Stm stm;
    public Exp exp;
    public ESEQ(Stm s, Exp e) { stm = s; exp = e; }
}