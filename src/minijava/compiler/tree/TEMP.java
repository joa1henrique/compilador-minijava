package minijava.compiler.tree;
import minijava.compiler.temp.Temp;

public class TEMP extends Exp {
    public Temp temp;
    public TEMP(Temp t) { temp = t; }
}