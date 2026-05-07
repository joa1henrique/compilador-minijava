package minijava.compiler.translate;

import minijava.compiler.tree.Stm;
import minijava.compiler.frame.Frame;

public class ProcFrag extends Frag {
    public Stm body;
    public Frame frame;

    public ProcFrag(Stm body, Frame frame) {
        this.body = body;
        this.frame = frame;
    }
}