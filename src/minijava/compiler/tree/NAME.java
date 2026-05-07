package minijava.compiler.tree;
import minijava.compiler.temp.Label;

public class NAME extends Exp {
    public Label label;
    public NAME(Label l) { label = l; }
}