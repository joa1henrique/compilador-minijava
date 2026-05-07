package minijava.compiler.translate;

import minijava.compiler.tree.CJUMP;
import minijava.compiler.temp.Label;

/**
 * Tradução de comparações (Ex: a < b).
 * Extende Cx porque seu objetivo principal é fazer um desvio condicional (CJUMP).
 */
public class RelCx extends Cx {
    int op;
    minijava.compiler.tree.Exp left, right;

    public RelCx(int o, minijava.compiler.tree.Exp l, minijava.compiler.tree.Exp r) {
        op = o;
        left = l;
        right = r;
    }

    @Override
    public minijava.compiler.tree.Stm unCx(Label t, Label f) {
        // Gera a instrução da IRTree que compara 'left' e 'right'
        // e pula para o label 't' se for verdade, ou 'f' se for falso.
        return new CJUMP(op, left, right, t, f);
    }
}