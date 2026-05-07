package minijava.compiler.tree;

public class SEQ extends Stm {
    public Stm left, right;
    public SEQ(Stm l, Stm r) { left = l; right = r; }
}