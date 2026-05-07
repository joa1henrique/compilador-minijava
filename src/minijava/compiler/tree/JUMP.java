package minijava.compiler.tree;
import minijava.compiler.temp.Label;
import java.util.List;

public class JUMP extends Stm {
    public Exp exp;
    public List<Label> targets;
    public JUMP(Exp e, List<Label> t) { exp = e; targets = t; }
    public JUMP(Label target) {
        this(new NAME(target), java.util.Collections.singletonList(target));
    }
}