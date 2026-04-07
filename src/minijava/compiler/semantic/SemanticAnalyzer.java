package minijava.compiler.semantic;

import minijava.compiler.ast.*;
import java.util.*;

/**
 * Analisador Semântico (Semantic Analyzer)
 * Implementa o padrão Visitor para análise semântica da AST
 */
public class SemanticAnalyzer implements Visitor<Void> {
    private final Program program;
    private boolean hadError = false;

    // Tabela de símbolos para classes
    private final Map<String, ClassInfo> classTable = new HashMap<>();
    
    // Contexto atual (classe, método, escopos de variáveis)
    private ClassInfo currentClass;
    private MethodInfo currentMethod;
    private final Stack<Map<String, Type>> scopeStack = new Stack<>();

    /**
     * Classe auxiliar para armazenar informações de classe
     */
    private static class ClassInfo {
        String name;
        String superClass;
        Map<String, Type> fields = new HashMap<>();
        Map<String, MethodInfo> methods = new HashMap<>();
    }

    /**
     * Classe auxiliar para armazenar informações de método
     */
    private static class MethodInfo {
        String name;
        Type returnType;
        List<String> paramNames = new ArrayList<>();
        Map<String, Type> params = new HashMap<>();
        Map<String, Type> locals = new HashMap<>();
    }

    public SemanticAnalyzer(Program program) {
        this.program = program;
    }

    public void analyze() {
        // Fase 1: Coleta de declarações de classe
        collectClassDeclarations();
        
        if (hadError) return;

        // Fase 2: Análise da AST
        program.accept(this);
    }

    public boolean hadError() {
        return hadError;
    }

    /**
     * Coleta todas as declarações de classe antes da análise semântica
     */
    private void collectClassDeclarations() {
        // Adiciona a classe principal
        if (program.mainClass != null) {
            ClassInfo mainClassInfo = new ClassInfo();
            mainClassInfo.name = program.mainClass.className.lexeme;
            classTable.put(mainClassInfo.name, mainClassInfo);
        }

        // Adiciona as outras classes
        for (ClassDecl classDecl : program.classes) {
            String className = classDecl.className.lexeme;
            
            if (classTable.containsKey(className)) {
                error("Classe '" + className + "' já foi declarada.");
                return;
            }

            ClassInfo classInfo = new ClassInfo();
            classInfo.name = className;
            classInfo.superClass = classDecl.superClassName != null ? classDecl.superClassName.lexeme : null;

            // Verifica se a superclasse existe (se houver)
            if (classInfo.superClass != null && !classTable.containsKey(classInfo.superClass)) {
                error("Superclasse '" + classInfo.superClass + "' não foi declarada.");
                return;
            }

            // Adiciona campos
            for (VarDecl field : classDecl.fields) {
                String fieldName = field.varName.lexeme;
                if (classInfo.fields.containsKey(fieldName)) {
                    error("Campo '" + fieldName + "' já foi declarado na classe '" + className + "'.");
                    return;
                }
                classInfo.fields.put(fieldName, field.type);
            }

            // Adiciona métodos
            for (MethodDecl method : classDecl.methods) {
                String methodName = method.methodName.lexeme;
                if (classInfo.methods.containsKey(methodName)) {
                    error("Método '" + methodName + "' já foi declarado na classe '" + className + "'.");
                    return;
                }

                MethodInfo methodInfo = new MethodInfo();
                methodInfo.name = methodName;
                methodInfo.returnType = method.returnType;

                // Adiciona parâmetros
                for (VarDecl param : method.parameters) {
                    methodInfo.paramNames.add(param.varName.lexeme);
                    methodInfo.params.put(param.varName.lexeme, param.type);
                }

                // Adiciona variáveis locais
                for (VarDecl local : method.locals) {
                    methodInfo.locals.put(local.varName.lexeme, local.type);
                }

                classInfo.methods.put(methodName, methodInfo);
            }

            classTable.put(className, classInfo);
        }
    }

    @Override
    public Void visitProgram(Program node) {
        if (node.mainClass != null) {
            node.mainClass.accept(this);
        }
        for (ClassDecl classDecl : node.classes) {
            classDecl.accept(this);
        }
        return null;
    }

    @Override
    public Void visitMainClass(MainClass node) {
        currentClass = classTable.get(node.className.lexeme);
        pushScope();
        
        if (node.mainStatement != null) {
            node.mainStatement.accept(this);
        }
        
        popScope();
        return null;
    }

    @Override
    public Void visitClassDecl(ClassDecl node) {
        currentClass = classTable.get(node.className.lexeme);
        
        for (MethodDecl method : node.methods) {
            method.accept(this);
        }
        
        return null;
    }

    @Override
    public Void visitMethodDecl(MethodDecl node) {
        currentMethod = currentClass.methods.get(node.methodName.lexeme);
        pushScope();
        
        // Adiciona parâmetros ao escopo
        for (Map.Entry<String, Type> param : currentMethod.params.entrySet()) {
            defineVariable(param.getKey(), param.getValue());
        }

        // Adiciona variáveis locais ao escopo
        for (Map.Entry<String, Type> local : currentMethod.locals.entrySet()) {
            defineVariable(local.getKey(), local.getValue());
        }

        // Analisa os comandos
        for (Statement stmt : node.statements) {
            stmt.accept(this);
        }

        // Verifica o tipo do retorno
        if (node.returnExpression != null) {
            node.returnExpression.accept(this);
            Type returnExprType = node.returnExpression.type;
            
            if (!typesCompatible(currentMethod.returnType, returnExprType)) {
                error("Tipo de retorno incompatível. Esperado: " + currentMethod.returnType + 
                      ", Obtido: " + returnExprType);
            }
        }

        popScope();
        return null;
    }

    @Override
    public Void visitVarDecl(VarDecl node) {
        return null;
    }

    @Override
    public Void visitType(Type node) {
        return null;
    }

    @Override
    public Void visitBlockStatement(BlockStatement node) {
        pushScope();
        for (Statement stmt : node.statements) {
            stmt.accept(this);
        }
        popScope();
        return null;
    }

    @Override
    public Void visitIfStatement(IfStatement node) {
        node.condition.accept(this);
        
        // Verifica se a condição é um booleano
        if (node.condition.type != null && !node.condition.type.equals(new Type(Type.BaseType.BOOLEAN))) {
            error("Condição do 'if' deve ser do tipo boolean.");
        }
        
        node.thenStatement.accept(this);
        
        if (node.elseStatement != null) {
            node.elseStatement.accept(this);
        }
        
        return null;
    }

    @Override
    public Void visitWhileStatement(WhileStatement node) {
        node.condition.accept(this);
        
        // Verifica se a condição é um booleano
        if (node.condition.type != null && !node.condition.type.equals(new Type(Type.BaseType.BOOLEAN))) {
            error("Condição do 'while' deve ser do tipo boolean.");
        }
        
        node.body.accept(this);
        return null;
    }

    @Override
    public Void visitAssignmentStatement(AssignmentStatement node) {
        Type varType = resolveType(node.varName.lexeme);
        
        if (varType == null) {
            error("Variável '" + node.varName.lexeme + "' não foi declarada.");
            return null;
        }

        node.value.accept(this);
        
        if (!typesCompatible(varType, node.value.type)) {
            error("Tipo incompatível em atribuição. Variável: " + varType + 
                  ", Valor: " + node.value.type);
        }
        
        return null;
    }

    @Override
    public Void visitArrayAssignmentStatement(ArrayAssignmentStatement node) {
        Type varType = resolveType(node.varName.lexeme);
        
        if (varType == null) {
            error("Variável '" + node.varName.lexeme + "' não foi declarada.");
            return null;
        }

        if (!varType.isArray) {
            error("Não é possível indexar uma variável que não é array.");
            return null;
        }

        node.index.accept(this);
        if (node.index.type != null && !node.index.type.equals(new Type(Type.BaseType.INT))) {
            error("Índice de array deve ser do tipo int.");
        }

        node.value.accept(this);
        
        Type elementType = new Type(varType.baseType, varType.className, false);
        if (!typesCompatible(elementType, node.value.type)) {
            error("Tipo incompatível em atribuição de array.");
        }
        
        return null;
    }

    @Override
    public Void visitPrintStatement(PrintStatement node) {
        node.expression.accept(this);
        return null;
    }

    @Override
    public Void visitReturnStatement(ReturnStatement node) {
        node.expression.accept(this);
        return null;
    }

    @Override
    public Void visitIntegerLiteral(IntegerLiteral node) {
        return null;
    }

    @Override
    public Void visitBooleanLiteral(BooleanLiteral node) {
        return null;
    }

    @Override
    public Void visitIdentifier(Identifier node) {
        Type resolvedType = resolveType(node.name.lexeme);
        
        if (resolvedType == null) {
            error("Identificador '" + node.name.lexeme + "' não foi declarado.");
            node.type = new Type(Type.BaseType.INT);  // Tipo padrão para evitar mais erros
            return null;
        }
        
        node.type = resolvedType;
        return null;
    }

    @Override
    public Void visitThis(This node) {
        if (currentClass != null) {
            node.type = new Type(currentClass.name);
        }
        return null;
    }

    @Override
    public Void visitNewObject(NewObject node) {
        String className = node.className.lexeme;
        
        if (!classTable.containsKey(className)) {
            error("Classe '" + className + "' não foi declarada.");
            node.type = new Type(className);
            return null;
        }
        
        node.type = new Type(className);
        return null;
    }

    @Override
    public Void visitNewArray(NewArray node) {
        node.sizeExpression.accept(this);
        
        if (node.sizeExpression.type != null && !node.sizeExpression.type.equals(new Type(Type.BaseType.INT))) {
            error("Tamanho do array deve ser do tipo int.");
        }
        
        return null;
    }

    @Override
    public Void visitBinaryOp(BinaryOp node) {
        node.left.accept(this);
        node.right.accept(this);
        
        // Verifica compatibilidade de tipos
        switch (node.operator) {
            case AND:
                if (node.left.type != null && !node.left.type.equals(new Type(Type.BaseType.BOOLEAN))) {
                    error("Operador '&&' espera operando esquerdo do tipo boolean.");
                }
                if (node.right.type != null && !node.right.type.equals(new Type(Type.BaseType.BOOLEAN))) {
                    error("Operador '&&' espera operando direito do tipo boolean.");
                }
                node.type = new Type(Type.BaseType.BOOLEAN);
                break;
            
            case LESS_THAN:
                if (node.left.type != null && !node.left.type.equals(new Type(Type.BaseType.INT))) {
                    error("Operador '<' espera operando esquerdo do tipo int.");
                }
                if (node.right.type != null && !node.right.type.equals(new Type(Type.BaseType.INT))) {
                    error("Operador '<' espera operando direito do tipo int.");
                }
                node.type = new Type(Type.BaseType.BOOLEAN);
                break;
            
            case PLUS:
            case MINUS:
            case TIMES:
                if (node.left.type != null && !node.left.type.equals(new Type(Type.BaseType.INT))) {
                    error("Operador aritmético espera operando esquerdo do tipo int.");
                }
                if (node.right.type != null && !node.right.type.equals(new Type(Type.BaseType.INT))) {
                    error("Operador aritmético espera operando direito do tipo int.");
                }
                node.type = new Type(Type.BaseType.INT);
                break;
        }
        
        return null;
    }

    @Override
    public Void visitUnaryOp(UnaryOp node) {
        node.operand.accept(this);
        
        if (node.operand.type != null && !node.operand.type.equals(new Type(Type.BaseType.BOOLEAN))) {
            error("Operador '!' espera operando do tipo boolean.");
        }
        
        return null;
    }

    @Override
    public Void visitMethodCall(MethodCall node) {
        node.object.accept(this);
        
        Type objectType = node.object.type;
        if (objectType == null) {
            error("Não foi possível determinar o tipo do objeto para chamada de método.");
            return null;
        }

        String methodName = node.methodName.lexeme;
        
        // Resolve o tipo do retorno do método
        if (objectType.baseType == Type.BaseType.OBJECT) {
            ClassInfo classInfo = classTable.get(objectType.className);
            if (classInfo == null) {
                error("Classe '" + objectType.className + "' não foi declarada.");
                return null;
            }

            MethodInfo methodInfo = classInfo.methods.get(methodName);
            if (methodInfo == null) {
                error("Método '" + methodName + "' não foi encontrado na classe '" + objectType.className + "'.");
                return null;
            }

            // Verifica argumentos
            if (node.arguments.size() != methodInfo.paramNames.size()) {
                error("Número de argumentos incorreto para método '" + methodName + "'.");
            }

            for (int i = 0; i < node.arguments.size(); i++) {
                Expression arg = node.arguments.get(i);
                arg.accept(this);
                
                if (i < methodInfo.paramNames.size()) {
                    String paramName = methodInfo.paramNames.get(i);
                    Type paramType = methodInfo.params.get(paramName);
                    
                    if (!typesCompatible(paramType, arg.type)) {
                        error("Tipo de argumento incompatível para método '" + methodName + "'.");
                    }
                }
            }

            node.type = methodInfo.returnType;
        } else if (objectType.baseType == Type.BaseType.INT && objectType.isArray) {
            // Arrays não têm métodos exceto .length
            if (!methodName.equals("length")) {
                error("Arrays não têm método '" + methodName + "'.");
            }
        }
        
        return null;
    }

    @Override
    public Void visitArrayAccess(ArrayAccess node) {
        node.array.accept(this);
        node.index.accept(this);
        
        if (node.array.type != null && !node.array.type.isArray) {
            error("Não é possível indexar uma variável que não é array.");
        }

        if (node.index.type != null && !node.index.type.equals(new Type(Type.BaseType.INT))) {
            error("Índice de array deve ser do tipo int.");
        }

        // Tipo resultante é o tipo base do array
        if (node.array.type != null) {
            node.type = new Type(node.array.type.baseType, node.array.type.className, false);
        }
        
        return null;
    }

    @Override
    public Void visitArrayLength(ArrayLength node) {
        node.array.accept(this);
        
        if (node.array.type != null && !node.array.type.isArray) {
            error("Não é possível acessar .length de uma variável que não é array.");
        }
        
        return null;
    }

    @Override
    public Void visitParenthesizedExpression(ParenthesizedExpression node) {
        node.expression.accept(this);
        node.type = node.expression.type;
        return null;
    }

    /**
     * Métodos auxiliares
     */

    private void pushScope() {
        scopeStack.push(new HashMap<>());
    }

    private void popScope() {
        if (!scopeStack.isEmpty()) {
            scopeStack.pop();
        }
    }

    private void defineVariable(String name, Type type) {
        if (!scopeStack.isEmpty()) {
            scopeStack.peek().put(name, type);
        }
    }

    private Type resolveType(String name) {
        // Procura nos escopos (de cima para baixo)
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            Type type = scopeStack.get(i).get(name);
            if (type != null) {
                return type;
            }
        }

        // Procura nos campos da classe
        if (currentClass != null && currentClass.fields.containsKey(name)) {
            return currentClass.fields.get(name);
        }

        return null;
    }

    private boolean typesCompatible(Type expected, Type actual) {
        if (expected == null || actual == null) {
            return true;  // Permitir se um dos tipos é desconhecido
        }
        return expected.equals(actual);
    }

    private void error(String message) {
        hadError = true;
        System.err.println("[Semantic Error] " + message);
    }
}

