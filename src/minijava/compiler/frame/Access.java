package minijava.compiler.frame;

import minijava.compiler.tree.Exp;

public abstract class Access {
    /**
     * Retorna a expressão da IRTree que representa o local de memória ou 
     * registrador deste acesso.
     * 
     * @param framePtr A expressão que aponta para o Frame Pointer (FP) atual.
     */
    public abstract Exp exp(Exp framePtr);
}