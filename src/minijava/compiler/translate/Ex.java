package minijava.compiler.translate;

import minijava.compiler.tree.*;
import minijava.compiler.temp.Label;

public class Ex extends Exp {
    minijava.compiler.tree.Exp exp;

    public Ex(minijava.compiler.tree.Exp e) { 
        exp = e; 
    }

    @Override
    public minijava.compiler.tree.Exp unEx() { 
        return exp; 
    }

    @Override
    public Stm unNx() { 
        return new EXPR(exp); 
    }

    @Override
    public Stm unCx(Label t, Label f) {
        // Se pedir para agir como condicional, verifica se é diferente de 0
        return new CJUMP(CJUMP.NE, exp, new CONST(0), t, f);
    }
}