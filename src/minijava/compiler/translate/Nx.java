package minijava.compiler.translate;

import minijava.compiler.tree.*;
import minijava.compiler.temp.Label;

public class Nx extends Exp {
    Stm stm;

    public Nx(Stm s) { 
        stm = s; 
    }

    @Override
    public minijava.compiler.tree.Exp unEx() { 
        return null; // Um comando puro não pode gerar um valor
    }

    @Override
    public Stm unNx() { 
        return stm; 
    }

    @Override
    public Stm unCx(Label t, Label f) { 
        return null; // Um comando puro não pode ser avaliado como verdadeiro ou falso
    }
}