package minijava.compiler.translate;

import minijava.compiler.temp.Label;
import minijava.compiler.temp.Temp;
import minijava.compiler.tree.*;

/**
 * Tradução de expressões condicionais (if-then-else).
 * Adapta o nó condicional para agir como Comando, Expressão ou Desvio.
 */
public class IfThenElseExp extends Exp {
    Exp cond, a, b;
    Label t = new Label();
    Label f = new Label();
    Label join = new Label();

    public IfThenElseExp(Exp cc, Exp aa, Exp bb) {
        cond = cc;
        a = aa;
        b = bb;
    }

    // Método auxiliar para não poluir o código com 'new SEQ' e tratar nulos
    private static Stm SEQ(Stm left, Stm right) {
        if (left == null) return right;
        if (right == null) return left;
        return new SEQ(left, right);
    }

    private static LABEL LABEL(Label l) { return new LABEL(l); }
    private static JUMP JUMP(Label l) { return new JUMP(l); }

    @Override
    public Stm unCx(Label tt, Label ff) {
        Stm aStm = a.unCx(tt, ff);
        Stm bStm = b.unCx(tt, ff);
        Stm condStm = cond.unCx(t, f);
        
        return SEQ(condStm, 
               SEQ(SEQ(LABEL(t), aStm), 
                   SEQ(LABEL(f), bStm)));
    }

    @Override
    public minijava.compiler.tree.Exp unEx() {
        minijava.compiler.tree.Exp aExp = a.unEx();
        if (aExp == null) return null;
        
        minijava.compiler.tree.Exp bExp = b.unEx();
        if (bExp == null) return null;
        
        Temp r = new Temp();
        return new ESEQ(
            SEQ(SEQ(cond.unCx(t, f),
                SEQ(SEQ(LABEL(t), 
                    SEQ(new MOVE(new TEMP(r), aExp), JUMP(join))),
                SEQ(LABEL(f), 
                    SEQ(new MOVE(new TEMP(r), bExp), JUMP(join))))),
            LABEL(join)),
            new TEMP(r)
        );
    }

    @Override
    public Stm unNx() {
        Stm aStm = a.unNx();
        if (aStm == null) t = join; 
        else aStm = SEQ(SEQ(LABEL(t), aStm), JUMP(join));
        
        Stm bStm = b.unNx();
        if (bStm == null) f = join; 
        else bStm = SEQ(SEQ(LABEL(f), bStm), JUMP(join));
        
        if (aStm == null && bStm == null) return cond.unNx();
        
        Stm condStm = cond.unCx(t, f);
        
        if (aStm == null) return SEQ(SEQ(condStm, bStm), LABEL(join));
        if (bStm == null) return SEQ(SEQ(condStm, aStm), LABEL(join));
        
        return SEQ(SEQ(condStm, SEQ(aStm, bStm)), LABEL(join));
    }
}