package minijava.compiler.translate;

import minijava.compiler.tree.Stm;
import minijava.compiler.temp.Label;

public abstract class Exp {
    // Computa e retorna um valor
    public abstract minijava.compiler.tree.Exp unEx();
    
    // Executa como comando (ignora o valor retornado)
    public abstract Stm unNx();
    
    // Executa como um desvio condicional (pula para t se verdadeiro, f se falso)
    public abstract Stm unCx(Label t, Label f);
}