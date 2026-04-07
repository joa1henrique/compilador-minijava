package minijava.compiler.ast;

/**
 * Interface Visitor para percorrer a AST.
 * Implementa o padrão Visitor para análise semântica e geração de código.
 */
public interface Visitor<T> {
    T visitProgram(Program node);
    T visitMainClass(MainClass node);
    T visitClassDecl(ClassDecl node);
    T visitMethodDecl(MethodDecl node);
    T visitVarDecl(VarDecl node);
    T visitType(Type node);
    T visitBlockStatement(BlockStatement node);
    T visitIfStatement(IfStatement node);
    T visitWhileStatement(WhileStatement node);
    T visitAssignmentStatement(AssignmentStatement node);
    T visitArrayAssignmentStatement(ArrayAssignmentStatement node);
    T visitPrintStatement(PrintStatement node);
    T visitReturnStatement(ReturnStatement node);
    T visitIntegerLiteral(IntegerLiteral node);
    T visitBooleanLiteral(BooleanLiteral node);
    T visitIdentifier(Identifier node);
    T visitThis(This node);
    T visitNewObject(NewObject node);
    T visitNewArray(NewArray node);
    T visitBinaryOp(BinaryOp node);
    T visitUnaryOp(UnaryOp node);
    T visitMethodCall(MethodCall node);
    T visitArrayAccess(ArrayAccess node);
    T visitArrayLength(ArrayLength node);
    T visitParenthesizedExpression(ParenthesizedExpression node);
}

