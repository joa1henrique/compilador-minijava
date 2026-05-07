package minijava.compiler.translate;

import minijava.compiler.tree.*;
import minijava.compiler.temp.Temp;
import minijava.compiler.temp.Label;

public abstract class Cx extends Exp {
    
    @Override
    public minijava.compiler.tree.Exp unEx() {
        Temp r = new Temp();
        Label t = new Label();
        Label f = new Label();
        
        // Converte um if/else em valor numérico (1 ou 0)
        return new ESEQ(
            new SEQ(new MOVE(new TEMP(r), new CONST(1)),
                new SEQ(unCx(t, f),
                    new SEQ(new LABEL(f),
                        new SEQ(new MOVE(new TEMP(r), new CONST(0)),
                            new LABEL(t))))),
            new TEMP(r));
    }

    @Override
    public Stm unNx() {
        Label join = new Label();
        return new SEQ(unCx(join, join), new LABEL(join));
    }

    @Override
    public abstract Stm unCx(Label t, Label f);
}