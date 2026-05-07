package minijava.compiler.frame;

import minijava.compiler.temp.Label;
import minijava.compiler.temp.Temp;
import minijava.compiler.tree.Exp;
import minijava.compiler.tree.Stm;
import java.util.List;

public abstract class Frame {
    public Label name;
    public List<Access> formals;

    /**
     * Cria um novo frame para uma função.
     */
    public abstract Frame newFrame(Label name, List<Boolean> formals);

    /**
     * Aloca uma nova variável local (no frame se escape for true, senão no registrador).
     */
    public abstract Access allocLocal(boolean escape);

    /**
     * Retorna o registrador do Frame Pointer (FP).
     */
    public abstract Temp FP();

    /**
     * Retorna o tamanho da palavra na arquitetura alvo (ex: 4 bytes).
     */
    public abstract int wordSize();

    /**
     * Retorna o registrador usado para armazenar o valor de retorno (RV).
     */
    public abstract Temp RV();

    /**
     * Adiciona instruções de prólogo e epílogo (mudança de visão) ao corpo da função.
     */
    public abstract Stm procEntryExit1(Stm body);

    /**
     * Cria uma chamada para uma função externa do sistema operacional/runtime.
     */
    public abstract Exp externalCall(String func, List<Exp> args);
}