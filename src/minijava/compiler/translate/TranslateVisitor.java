package minijava.compiler.translate;

import minijava.compiler.ast.*;
import minijava.compiler.tree.BINOP;
import minijava.compiler.tree.CALL;
import minijava.compiler.tree.CJUMP;
import minijava.compiler.tree.CONST;
import minijava.compiler.tree.ESEQ;
import minijava.compiler.tree.EXPR;
import minijava.compiler.tree.JUMP;
import minijava.compiler.tree.LABEL;
import minijava.compiler.tree.MEM;
import minijava.compiler.tree.MOVE;
import minijava.compiler.tree.NAME;
import minijava.compiler.tree.SEQ;
import minijava.compiler.tree.TEMP;
import minijava.compiler.temp.Label;
import minijava.compiler.temp.Temp;
import minijava.compiler.frame.Access;
import minijava.compiler.frame.Frame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Visita a AST gerando a Árvore de Representação Intermediária (IRTree).
 */
public class TranslateVisitor implements Visitor<Exp> {
    
    private Frag fragList = null;
    
    // O Frame atua como uma "fábrica" para a arquitetura alvo (ex: MIPS, x86)
    private Frame frameFactory; 
    private Frame currentFrame = null;
    
    // Tabela de símbolos local para mapear o nome da variável ao seu endereço de memória (Access)
    private Map<String, Access> varEnv;

    public TranslateVisitor(Frame frameFactory) {
        this.frameFactory = frameFactory;
    }

    public Frag getResult() {
        return fragList;
    }

    public void addFrag(Frag frag) {
        frag.next = fragList;
        fragList = frag;
    }

    @Override
    public Exp visitProgram(Program node) {
        if (node.mainClass != null) node.mainClass.accept(this);
        for (ClassDecl c : node.classes) c.accept(this);
        return null;
    }

    @Override
    public Exp visitMainClass(MainClass node) {
        // 1. Cria um novo Frame para a Main (com 1 parâmetro: a string args[])
        List<Boolean> formals = new ArrayList<>();
        formals.add(false); // Assume que o array de args não escapa
        currentFrame = frameFactory.newFrame(new Label("main"), formals);
        varEnv = new HashMap<>();

        // 2. Visita o corpo da main
        Exp body = node.mainStatement.accept(this);

        // 3. Empacota o corpo da main num Fragmento (sem retorno)
        minijava.compiler.tree.Stm bodyStm = currentFrame.procEntryExit1(body.unNx());
        addFrag(new ProcFrag(bodyStm, currentFrame));
        
        return null;
    }

    @Override
    public Exp visitClassDecl(ClassDecl node) {
        for (MethodDecl m : node.methods) m.accept(this);
        return null;
    }

    @Override
    public Exp visitMethodDecl(MethodDecl node) {
        varEnv = new HashMap<>();
        List<Boolean> formals = new ArrayList<>();
        
        // O primeiro parâmetro (escondido) de todo método OO é o ponteiro 'this'
        formals.add(false); 
        
        for (VarDecl param : node.parameters) {
            formals.add(false); // Para simplificar, assumimos que parâmetros não escapam
        }

        // 1. Cria o Frame do método
        String fullName = node.methodName.lexeme; // O ideal seria Classe$Metodo
        currentFrame = frameFactory.newFrame(new Label(fullName), formals);

        // 2. Mapeia o 'this' e os parâmetros para a memória (Access)
        int formalIndex = 0;
        varEnv.put("this", currentFrame.formals.get(formalIndex++));
        for (VarDecl param : node.parameters) {
            varEnv.put(param.varName.lexeme, currentFrame.formals.get(formalIndex++));
        }

        // 3. Aloca variáveis locais no Frame
        for (VarDecl local : node.locals) {
            varEnv.put(local.varName.lexeme, currentFrame.allocLocal(false));
        }

        // 4. Traduz os comandos do método (agrupando-os com SEQ)
        minijava.compiler.tree.Stm stm = null;
        for (Statement s : node.statements) {
            minijava.compiler.tree.Stm sStm = s.accept(this).unNx();
            if (stm == null) stm = sStm;
            else stm = new SEQ(stm, sStm);
        }

        // 5. Traduz a expressão de retorno e a move para o registrador de retorno (RV)
        Exp retExp = node.returnExpression.accept(this);
        minijava.compiler.tree.Stm retStm = new MOVE(new TEMP(currentFrame.RV()), retExp.unEx());

        if (stm != null) stm = new SEQ(stm, retStm);
        else stm = retStm;

        // 6. Finaliza o fragmento
        addFrag(new ProcFrag(currentFrame.procEntryExit1(stm), currentFrame));

        return null;
    }

    @Override
    public Exp visitVarDecl(VarDecl node) {
        // Alocação já tratada no visitMethodDecl
        return null;
    }

    @Override
    public Exp visitIdentifier(Identifier node) {
        // Busca onde a variável está alocada no Frame atual
        Access acc = varEnv.get(node.name.lexeme);
        if (acc != null) {
            // O Access sabe se deve gerar um MEM() ou um TEMP(), bastando passar o Frame Pointer (FP)
            return new Ex(acc.exp(new TEMP(currentFrame.FP())));
        }
        // Se for atributo da classe, a tradução é diferente (requer cálculo de offset do objeto)
        // Para simplificar essa etapa, omitimos a busca no heap aqui.
        return null; 
    }

    @Override
    public Exp visitThis(This node) {
        Access acc = varEnv.get("this");
        return new Ex(acc.exp(new TEMP(currentFrame.FP())));
    }

    @Override
    public Exp visitAssignmentStatement(AssignmentStatement node) {
        Exp src = node.value.accept(this);
        Access acc = varEnv.get(node.varName.lexeme);
        if (acc != null) {
            minijava.compiler.tree.Exp dst = acc.exp(new TEMP(currentFrame.FP()));
            return new Nx(new MOVE(dst, src.unEx()));
        }
        return new Nx(new EXPR(src.unEx())); // Fallback
    }

    @Override
    public Exp visitIfStatement(IfStatement node) {
        Exp cond = node.condition.accept(this);
        Exp thenExp = node.thenStatement.accept(this);
        Exp elseExp = node.elseStatement != null ? node.elseStatement.accept(this) : new Nx(null);
        return new IfThenElseExp(cond, thenExp, elseExp);
    }

    @Override
    public Exp visitWhileStatement(WhileStatement node) {
        Exp cond = node.condition.accept(this);
        Exp body = node.body.accept(this);
        
        Label test = new Label();
        Label done = new Label();
        Label bodyLabel = new Label();
        
        return new Nx(
            new SEQ(new LABEL(test),
                new SEQ(cond.unCx(bodyLabel, done),
                    new SEQ(new LABEL(bodyLabel),
                        new SEQ(body.unNx(),
                            new SEQ(new JUMP(test),
                                new LABEL(done))))))
        );
    }

    @Override
    public Exp visitBinaryOp(BinaryOp node) {
        Exp left = node.left.accept(this);
        Exp right = node.right.accept(this);
        
        switch (node.operator) {
            case PLUS: return new Ex(new BINOP(BINOP.PLUS, left.unEx(), right.unEx()));
            case MINUS: return new Ex(new BINOP(BINOP.MINUS, left.unEx(), right.unEx()));
            case TIMES: return new Ex(new BINOP(BINOP.MUL, left.unEx(), right.unEx()));
            case AND: return new Ex(new BINOP(BINOP.AND, left.unEx(), right.unEx()));
            case LESS_THAN: return new RelCx(CJUMP.LT, left.unEx(), right.unEx());
            default: return null;
        }
    }

    @Override
    public Exp visitMethodCall(MethodCall node) {
        minijava.compiler.tree.Exp objExp = node.object.accept(this).unEx();
        List<minijava.compiler.tree.Exp> irArgs = new ArrayList<>();
        
        irArgs.add(objExp); // O objeto ('this') é o primeiro argumento
        for (Expression arg : node.arguments) {
            irArgs.add(arg.accept(this).unEx());
        }
        
        Label funcLabel = new Label(node.methodName.lexeme);
        return new Ex(new CALL(new NAME(funcLabel), irArgs));
    }

    @Override
    public Exp visitPrintStatement(PrintStatement node) {
        Exp expr = node.expression.accept(this);
        List<minijava.compiler.tree.Exp> args = new ArrayList<>();
        args.add(expr.unEx());
        // Em MiniJava, imprimir é uma chamada externa ao sistema
        return new Nx(new EXPR(currentFrame.externalCall("printInt", args)));
    }

    @Override
    public Exp visitIntegerLiteral(IntegerLiteral node) {
        return new Ex(new CONST(node.value));
    }

    @Override
    public Exp visitBooleanLiteral(BooleanLiteral node) {
        return new Ex(new CONST(node.value ? 1 : 0));
    }

    @Override
    public Exp visitBlockStatement(BlockStatement node) {
        minijava.compiler.tree.Stm stm = null;
        for (Statement s : node.statements) {
            minijava.compiler.tree.Stm sStm = s.accept(this).unNx();
            if (stm == null) stm = sStm;
            else stm = new SEQ(stm, sStm);
        }
        return stm != null ? new Nx(stm) : new Nx(null);
    }
    
    @Override
    public Exp visitType(Type node) { 
        // Tipos não geram código na Árvore de Representação Intermediária.
        // Toda a checagem de tipos já foi feita pelo SemanticAnalyzer.
        return null; 
    }

    @Override
    public Exp visitArrayAssignmentStatement(ArrayAssignmentStatement node) {
        // 1. Busca onde está o ponteiro base do array na memória local
        Access acc = varEnv.get(node.varName.lexeme);
        minijava.compiler.tree.Exp arrayBase;
        
        if (acc != null) {
            arrayBase = acc.exp(new TEMP(currentFrame.FP()));
        } else {
            // Fallback genérico caso a variável não esteja no escopo local
            arrayBase = new TEMP(new minijava.compiler.temp.Temp());
        }

        // 2. Avalia o índice e o valor que queremos guardar
        Exp index = node.index.accept(this);
        Exp value = node.value.accept(this);

        // 3. Calcula o endereço exato na memória
        // Endereço = Base + ((Indice + 1) * Tamanho_da_Palavra)
        minijava.compiler.tree.Exp realIndex = new BINOP(BINOP.PLUS, index.unEx(), new CONST(1));
        minijava.compiler.tree.Exp offsetBytes = new BINOP(BINOP.MUL, realIndex, new CONST(currentFrame.wordSize()));
        minijava.compiler.tree.Exp enderecoNaMemoria = new BINOP(BINOP.PLUS, arrayBase, offsetBytes);

        // 4. MOVE o valor calculado para a Memória (MEM) calculada
        return new Nx(new MOVE(new MEM(enderecoNaMemoria), value.unEx()));
    }

    @Override
    public Exp visitReturnStatement(ReturnStatement node) {
        // 1. Avalia a expressão que está sendo retornada
        Exp expr = node.expression.accept(this);
        
        // 2. Move o resultado para o registrador especial de Retorno (RV)
        // Como é um comando isolado, envelopamos num Nx (No Result)
        return new Nx(new MOVE(new TEMP(currentFrame.RV()), expr.unEx()));
    }

    @Override
    public Exp visitNewObject(NewObject node) {
        // No MiniJava, alocar um objeto significa pedir ao SO um bloco de memória.
        // O tamanho exato deveria ser (Número_de_Atributos * Word_Size).
        // Como o Fac() do Factorial.java não tem atributos (campos), podemos 
        // alocar um tamanho mínimo simbólico de 4 bytes (1 palavra) para o ponteiro.
        
        int classSizeEmBytes = currentFrame.wordSize(); // Mínimo seguro
        
        List<minijava.compiler.tree.Exp> args = new ArrayList<>();
        args.add(new CONST(classSizeEmBytes));
        
        // Chamamos a função do sistema para alocar memória e retornar o endereço
        return new Ex(currentFrame.externalCall("_allocRecord", args));
    }

    @Override
    public Exp visitParenthesizedExpression(ParenthesizedExpression node) { 
        // Esse você já tinha feito certinho! Os parênteses servem só para a árvore sintática, 
        // na hora da tradução nós apenas repassamos a expressão interna adiante.
        return node.expression.accept(this); 
    }

    @Override
    public Exp visitUnaryOp(UnaryOp node) {
        // Em MiniJava, o único UnaryOp é o '!' (NOT lógico).
        // Em código de máquina, True = 1 e False = 0.
        // Fazer a negação '!x' é matematicamente a mesma coisa que subtrair: '1 - x'.
        Exp operando = node.operand.accept(this);
        return new Ex(new BINOP(BINOP.MINUS, new CONST(1), operando.unEx()));
    }

    @Override
    public Exp visitNewArray(NewArray node) {
        // Traduz: new int[size]
        // Para criar um array, pedimos ao Sistema Operacional para alocar memória.
        // O livro do Appel (padrão do MiniJava) chama uma função externa "initArray".
        Exp size = node.sizeExpression.accept(this);
        
        java.util.List<minijava.compiler.tree.Exp> args = new java.util.ArrayList<>();
        args.add(size.unEx());
        
        return new Ex(currentFrame.externalCall("initArray", args));
    }

    @Override
    public Exp visitArrayLength(ArrayLength node) {
        // Traduz: array.length
        // No padrão do compilador, quando alocamos o array, o tamanho dele fica 
        // guardado no primeiro "slot" de memória do ponteiro (índice 0).
        Exp array = node.array.accept(this);
        return new Ex(new MEM(array.unEx()));
    }

    @Override
    public Exp visitArrayAccess(ArrayAccess node) {
        // Traduz: array[index]
        Exp array = node.array.accept(this);
        Exp index = node.index.accept(this);
        
        // Em Assembly, não existe "posição 3 do array". Existe cálculo de ponteiro!
        // Endereço exato = Ponteiro Base + ((Índice + 1) * Tamanho_da_Palavra)
        // (Somamos +1 no índice para pular o slot 0, onde está guardado o 'length' do array)
        
        minijava.compiler.tree.Exp realIndex = new BINOP(BINOP.PLUS, index.unEx(), new CONST(1));
        minijava.compiler.tree.Exp offsetEmBytes = new BINOP(BINOP.MUL, realIndex, new CONST(currentFrame.wordSize()));
        minijava.compiler.tree.Exp enderecoNaMemoria = new BINOP(BINOP.PLUS, array.unEx(), offsetEmBytes);
        
        // Com o endereço exato em mãos, geramos um nó de acesso à memória (MEM)
        return new Ex(new MEM(enderecoNaMemoria));
    }
}