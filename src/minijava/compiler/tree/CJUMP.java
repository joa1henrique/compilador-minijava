package minijava.compiler.tree;
import minijava.compiler.temp.Label;

public class CJUMP extends Stm {
    public int relop;
    public Exp left, right;
    public Label iftrue, iffalse;
    
    public final static int EQ=0, NE=1, LT=2, GT=3, LE=4, GE=5, 
                            ULT=6, ULE=7, UGT=8, UGE=9;

    public CJUMP(int rel, Exp l, Exp r, Label t, Label f) {
        relop = rel; left = l; right = r; iftrue = t; iffalse = f;
    }
}