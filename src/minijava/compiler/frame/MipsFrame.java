package minijava.compiler.frame;

import minijava.compiler.temp.Label;
import minijava.compiler.temp.Temp;
import minijava.compiler.tree.BINOP;
import minijava.compiler.tree.CALL;
import minijava.compiler.tree.CONST;
import minijava.compiler.tree.Exp;
import minijava.compiler.tree.MEM;
import minijava.compiler.tree.NAME;
import minijava.compiler.tree.Stm;

import java.util.ArrayList;
import java.util.List;

public class MipsFrame extends Frame {
    
    private int offset = 0; // Controla o tamanho do Frame atual
    private static final int WORD_SIZE = 4; // Em MIPS, uma palavra tem 4 bytes
    
    // Registradores Especiais do MIPS
    private static final Temp FP = new Temp(); // Frame Pointer ($fp) - Base da memória do método
    private static final Temp RV = new Temp(); // Return Value ($v0) - Onde fica o 'return'

    // Construtor vazio usado apenas como "Fábrica" na classe Main
    public MipsFrame() {} 

    // Construtor real que cria o Frame de um método específico
    private MipsFrame(Label n, List<Boolean> f) {
        this.name = n;
        this.formals = new ArrayList<>();
        // Para cada parâmetro recebido, aloca um espaço
        for (Boolean escape : f) {
            this.formals.add(allocLocal(escape));
        }
    }

    @Override
    public Frame newFrame(Label name, List<Boolean> formals) {
        return new MipsFrame(name, formals);
    }

    @Override
    public Access allocLocal(boolean escape) {
        // Para simplificar a implementação, alocaremos TODAS as variáveis na memória (Pilha/Frame).
        // (Num compilador otimizado, algumas ficariam apenas em registradores).
        offset -= WORD_SIZE; // Desce 4 bytes na pilha para criar a "gaveta"
        return new InFrame(offset); 
    }

    @Override
    public Temp FP() { return FP; }

    @Override
    public Temp RV() { return RV; }

    @Override
    public int wordSize() { return WORD_SIZE; }

    @Override
    public Exp externalCall(String funcName, List<Exp> args) {
        // No simulador SPIM (do MIPS), chamadas do sistema recebem um rótulo especial
        return new CALL(new NAME(new Label(funcName)), args);
    }

    @Override
    public Stm procEntryExit1(Stm body) {
        // Numa etapa avançada, aqui adicionaríamos os comandos para salvar/restaurar 
        // os registradores antigos na pilha. Por enquanto, retornamos o corpo do método.
        return body;
    }
    
    // =========================================================================
    // Classe Interna que implementa o Acesso à Memória de fato
    // =========================================================================
    private class InFrame extends Access {
        int localOffset;
        
        public InFrame(int offset) { 
            this.localOffset = offset; 
        }
        
        // Quando o TranslateVisitor tentar ler a variável, ele chama este método:
        @Override
        public Exp exp(Exp framePtr) {
            // Em MIPS, a variável na memória é: MEMória(FramePointer + offset)
            return new MEM(new BINOP(BINOP.PLUS, framePtr, new CONST(localOffset)));
        }
    }
}