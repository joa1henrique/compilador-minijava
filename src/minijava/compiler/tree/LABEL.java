package minijava.compiler.tree;
import minijava.compiler.temp.Label;

public class LABEL extends Stm {
    public Label label;
    public LABEL(Label l) { label = l; }
}