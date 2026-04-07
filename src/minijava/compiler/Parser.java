package minijava.compiler;

import minijava.compiler.ast.*;
import java.util.List;
import java.util.ArrayList;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private boolean hadError = false;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private static class ParseError extends RuntimeException {}

    /**
     * Faz o parsing e retorna a AST (raiz do programa)
     */
    public Program parseProgram() {
        try {
            return program();
        } catch (ParseError error) {
            hadError = true;
            synchronize();
            return null;
        }
    }

    public boolean hadError() {
        return hadError;
    }

    private Program program() {
        MainClass mainClass = mainClass();
        List<ClassDecl> classes = new ArrayList<>();
        
        while (!isAtEnd()) {
            classes.add(classDeclaration());
        }
        
        return new Program(mainClass, classes);
    }

    private MainClass mainClass() {
        consume(TokenType.CLASS, "Expect 'class' keyword.");
        Token className = consume(TokenType.IDENTIFIER, "Expect class name.");
        consume(TokenType.LEFT_BRACE, "Expect '{' after class name.");
        consume(TokenType.PUBLIC, "Expect 'public' keyword.");
        consume(TokenType.STATIC, "Expect 'static' keyword.");
        consume(TokenType.VOID, "Expect 'void' keyword.");
        consume(TokenType.MAIN, "Expect 'main' keyword.");
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'main'.");
        consume(TokenType.STRING, "Expect 'String' keyword.");
        consume(TokenType.LEFT_BRACKET, "Expect '['.");
        consume(TokenType.RIGHT_BRACKET, "Expect ']'.");
        Token argName = consume(TokenType.IDENTIFIER, "Expect argument name.");
        consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.");
        consume(TokenType.LEFT_BRACE, "Expect '{' in main method.");

        Statement mainStatement = statement();

        consume(TokenType.RIGHT_BRACE, "Expect '}' after main method.");
        consume(TokenType.RIGHT_BRACE, "Expect '}' after class body.");
        
        return new MainClass(className, argName, mainStatement);
    }

    private ClassDecl classDeclaration() {
        consume(TokenType.CLASS, "Expect 'class' keyword.");
        Token className = consume(TokenType.IDENTIFIER, "Expect class name.");
        
        Token superClassName = null;
        if (match(TokenType.EXTENDS)) {
            superClassName = consume(TokenType.IDENTIFIER, "Expect superclass name.");
        }
        
        consume(TokenType.LEFT_BRACE, "Expect '{' after class name.");

        List<VarDecl> fields = new ArrayList<>();
        List<MethodDecl> methods = new ArrayList<>();

        while (check(TokenType.PUBLIC) || check(TokenType.INT) || check(TokenType.BOOLEAN) || check(TokenType.IDENTIFIER)) {
            if (match(TokenType.PUBLIC)) {
                methods.add(methodDeclaration());
            } else {
                Type fieldType = type();
                Token fieldName = consume(TokenType.IDENTIFIER, "Expect variable name.");
                consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
                fields.add(new VarDecl(fieldType, fieldName));
            }
        }
        
        consume(TokenType.RIGHT_BRACE, "Expect '}' after class body.");
        
        return new ClassDecl(className, superClassName, fields, methods);
    }
    
    private MethodDecl methodDeclaration() {
        Type returnType = type();
        Token methodName = consume(TokenType.IDENTIFIER, "Expect method name.");
        consume(TokenType.LEFT_PAREN, "Expect '('.");
        
        List<VarDecl> parameters = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            parameters = formalList();
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')'.");
        consume(TokenType.LEFT_BRACE, "Expect '{'.");

        List<VarDecl> locals = new ArrayList<>();
        while (isVarDeclaration()) {
            locals.add(varDeclaration());
        }

        List<Statement> statements = new ArrayList<>();
        while (!check(TokenType.RETURN) && !check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(statement());
        }

        consume(TokenType.RETURN, "Expect 'return'.");
        Expression returnExpression = expression();
        consume(TokenType.SEMICOLON, "Expect ';'.");
        consume(TokenType.RIGHT_BRACE, "Expect '}'.");
        
        return new MethodDecl(returnType, methodName, parameters, locals, statements, returnExpression);
    }

    private VarDecl varDeclaration() {
        Type varType = type();
        Token varName = consume(TokenType.IDENTIFIER, "Expect variable name.");
        consume(TokenType.SEMICOLON, "Expect ';'.");
        return new VarDecl(varType, varName);
    }

    private List<VarDecl> formalList() {
        List<VarDecl> parameters = new ArrayList<>();
        
        Type paramType = type();
        Token paramName = consume(TokenType.IDENTIFIER, "Expect parameter name.");
        parameters.add(new VarDecl(paramType, paramName));
        
        while (match(TokenType.COMMA)) {
            paramType = type();
            paramName = consume(TokenType.IDENTIFIER, "Expect parameter name.");
            parameters.add(new VarDecl(paramType, paramName));
        }
        
        return parameters;
    }

    private Type type() {
        if (match(TokenType.INT)) {
            if (match(TokenType.LEFT_BRACKET)) {
                consume(TokenType.RIGHT_BRACKET, "Expect ']' for int array.");
                return new Type(Type.BaseType.INT, true);
            }
            return new Type(Type.BaseType.INT);
        } else if (match(TokenType.BOOLEAN)) {
            return new Type(Type.BaseType.BOOLEAN);
        } else if (match(TokenType.IDENTIFIER)) {
            String className = previous().lexeme;
            return new Type(className);
        } else {
            error(peek(), "Expect validation type.");
            return new Type(Type.BaseType.INT);  // Tipo padrão
        }
    }
    
    private TokenType peekNext() {
        if (current + 1 >= tokens.size()) return TokenType.EOF;
        return tokens.get(current + 1).type;
    }

    private Statement statement() {
        try {
            if (match(TokenType.LEFT_BRACE)) {
                List<Statement> statements = new ArrayList<>();
                while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
                    statements.add(statement());
                }
                consume(TokenType.RIGHT_BRACE, "Expect '}'.");
                return new BlockStatement(statements);
            } else if (match(TokenType.IF)) {
                consume(TokenType.LEFT_PAREN, "Expect '('.");
                Expression condition = expression();
                consume(TokenType.RIGHT_PAREN, "Expect ')'.");
                Statement thenStatement = statement();
                Statement elseStatement = null;
                if (match(TokenType.ELSE)) {
                    elseStatement = statement();
                }
                return new IfStatement(condition, thenStatement, elseStatement);
            } else if (match(TokenType.WHILE)) {
                consume(TokenType.LEFT_PAREN, "Expect '('.");
                Expression condition = expression();
                consume(TokenType.RIGHT_PAREN, "Expect ')'.");
                Statement body = statement();
                return new WhileStatement(condition, body);
            } else if (match(TokenType.IDENTIFIER)) {
                String lexeme = previous().lexeme;
                
                // Trata System.out.println(expr)
                if (lexeme.equals("System")) {
                    if (match(TokenType.DOT)) {
                        if (match(TokenType.IDENTIFIER) && previous().lexeme.equals("out")) {
                            if (match(TokenType.DOT)) {
                                if (match(TokenType.IDENTIFIER) && previous().lexeme.equals("println")) {
                                    consume(TokenType.LEFT_PAREN, "Expect '('.");
                                    Expression expr = expression();
                                    consume(TokenType.RIGHT_PAREN, "Expect ')'.");
                                    consume(TokenType.SEMICOLON, "Expect ';'.");
                                    return new PrintStatement(expr);
                                }
                            }
                        }
                    }
                    throw error(previous(), "Expect 'System.out.println'.");
                }

                Token varName = previous();
                
                // Trata atribuição simples ou atribuição em array
                if (match(TokenType.ASSIGN)) {
                    Expression value = expression();
                    consume(TokenType.SEMICOLON, "Expect ';'.");
                    return new AssignmentStatement(varName, value);
                } else if (match(TokenType.LEFT_BRACKET)) {
                    Expression index = expression();
                    consume(TokenType.RIGHT_BRACKET, "Expect ']'.");
                    consume(TokenType.ASSIGN, "Expect '='.");
                    Expression value = expression();
                    consume(TokenType.SEMICOLON, "Expect ';'.");
                    return new ArrayAssignmentStatement(varName, index, value);
                } else {
                    throw error(previous(), "Expect assignment.");
                }
            } else {
                throw error(peek(), "Expect statement.");
            }
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Expression expression() {
        return andExpression();
    }
    
    private Expression andExpression() {
        Expression expr = relationalExpression();
        
        while (match(TokenType.AND)) {
            BinaryOp.Operator op = BinaryOp.Operator.AND;
            Expression right = relationalExpression();
            expr = new BinaryOp(expr, op, right);
        }
        
        return expr;
    }
    
    private Expression relationalExpression() {
        Expression expr = additiveExpression();
        
        while (match(TokenType.LESS_THAN)) {
            BinaryOp.Operator op = BinaryOp.Operator.LESS_THAN;
            Expression right = additiveExpression();
            expr = new BinaryOp(expr, op, right);
        }
        
        return expr;
    }
    
    private Expression additiveExpression() {
        Expression expr = multiplicativeExpression();
        
        while (match(TokenType.PLUS) || match(TokenType.MINUS)) {
            TokenType opType = previous().type;
            BinaryOp.Operator op = opType == TokenType.PLUS ? BinaryOp.Operator.PLUS : BinaryOp.Operator.MINUS;
            Expression right = multiplicativeExpression();
            expr = new BinaryOp(expr, op, right);
        }
        
        return expr;
    }
     
    private Expression multiplicativeExpression() {
        Expression expr = notExpression();
        
        while (match(TokenType.TIMES)) {
            BinaryOp.Operator op = BinaryOp.Operator.TIMES;
            Expression right = notExpression();
            expr = new BinaryOp(expr, op, right);
        }
        
        return expr;
    }

    private Expression notExpression() {
        if (match(TokenType.NOT)) {
            Expression operand = notExpression();
            return new UnaryOp(UnaryOp.Operator.NOT, operand);
        } else {
            return term();
        }
    }
    
    private Expression term() {
        Expression expr = primary();
        
        while (true) {
            if (match(TokenType.LEFT_BRACKET)) {
                Expression index = expression();
                consume(TokenType.RIGHT_BRACKET, "Expect ']'.");
                expr = new ArrayAccess(expr, index);
            } else if (match(TokenType.DOT)) {
                if (match(TokenType.LENGTH)) {
                    expr = new ArrayLength(expr);
                } else {
                    Token methodName = consume(TokenType.IDENTIFIER, "Expect method name.");
                    consume(TokenType.LEFT_PAREN, "Expect '('.");
                    List<Expression> args = new ArrayList<>();
                    if (!check(TokenType.RIGHT_PAREN)) {
                        args.add(expression());
                        while (match(TokenType.COMMA)) {
                            args.add(expression());
                        }
                    }
                    consume(TokenType.RIGHT_PAREN, "Expect ')'.");
                    expr = new MethodCall(expr, methodName, args);
                }
            } else {
                break;
            }
        }
        
        return expr;
    }

    private Expression primary() {
        if (match(TokenType.INTEGER_LITERAL)) {
            int value = Integer.parseInt(previous().lexeme);
            return new IntegerLiteral(value);
        } else if (match(TokenType.TRUE)) {
            return new BooleanLiteral(true);
        } else if (match(TokenType.FALSE)) {
            return new BooleanLiteral(false);
        } else if (match(TokenType.THIS)) {
            return new This(previous());
        } else if (match(TokenType.NEW)) {
            if (match(TokenType.INT)) {
                consume(TokenType.LEFT_BRACKET, "Expect '['.");
                Expression sizeExpr = expression();
                consume(TokenType.RIGHT_BRACKET, "Expect ']'.");
                return new NewArray(sizeExpr);
            } else {
                Token className = consume(TokenType.IDENTIFIER, "Expect class name.");
                consume(TokenType.LEFT_PAREN, "Expect '('.");
                consume(TokenType.RIGHT_PAREN, "Expect ')'.");
                return new NewObject(className);
            }
        } else if (match(TokenType.LEFT_PAREN)) {
            Expression expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')'.");
            return new ParenthesizedExpression(expr);
        } else if (match(TokenType.IDENTIFIER)) {
            return new Identifier(previous());
        } else {
            throw error(peek(), "Expect expression.");
        }
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        hadError = true;
        System.err.println("[Line " + token.line + "] Error at '" + token.lexeme + "': " + message);
        return new ParseError();
    }

    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;
            switch (peek().type) {
                case CLASS:
                case PUBLIC:
                case STATIC:
                case VOID:
                case MAIN:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            advance();
        }
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isVarDeclaration() {
        if (check(TokenType.INT) || check(TokenType.BOOLEAN)) {
            return true;
        }
        if (check(TokenType.IDENTIFIER)) {
            if (peekNext() == TokenType.IDENTIFIER) {
                return true;
            }
        }
        return false;
    }
}