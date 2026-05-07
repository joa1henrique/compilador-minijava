package minijava.compiler.tree;
import java.util.List;

public class CALL extends Exp {
    public Exp func;
    public List<Exp> args;
    public CALL(Exp f, List<Exp> a) { func = f; args = a; }
}