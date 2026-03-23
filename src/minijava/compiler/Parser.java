package minijava.compiler;

import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private boolean hadError = false;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private static class ParseError extends RuntimeException {}

    public void parse() {
        try {
            program();
        } catch (ParseError error) {
            hadError = true;
            // Top level error
            synchronize();
        }
    }

    public boolean hadError() {
        return hadError;
    }

    private void program() {
        mainClass();
        while (!isAtEnd()) {
            classDeclaration();
        }
    }

    private void mainClass() {
        consume(TokenType.CLASS, "Expect 'class' keyword.");
        consume(TokenType.IDENTIFIER, "Expect class name.");
        consume(TokenType.LEFT_BRACE, "Expect '{' after class name.");
        consume(TokenType.PUBLIC, "Expect 'public' keyword.");
        consume(TokenType.STATIC, "Expect 'static' keyword.");
        consume(TokenType.VOID, "Expect 'void' keyword.");
        consume(TokenType.MAIN, "Expect 'main' keyword.");
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'main'.");
        consume(TokenType.STRING, "Expect 'String' keyword.");
        consume(TokenType.LEFT_BRACKET, "Expect '['.");
        consume(TokenType.RIGHT_BRACKET, "Expect ']'.");
        consume(TokenType.IDENTIFIER, "Expect argument name.");
        consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.");
        consume(TokenType.LEFT_BRACE, "Expect '{' in main method.");

        statement();

        consume(TokenType.RIGHT_BRACE, "Expect '}' after main method.");
        consume(TokenType.RIGHT_BRACE, "Expect '}' after class body.");
    }

    private void classDeclaration() {
        consume(TokenType.CLASS, "Expect 'class' keyword.");
        consume(TokenType.IDENTIFIER, "Expect class name.");
        if (match(TokenType.EXTENDS)) {
            consume(TokenType.IDENTIFIER, "Expect superclass name.");
        }
        consume(TokenType.LEFT_BRACE, "Expect '{' after class name.");

        while (check(TokenType.PUBLIC) || check(TokenType.INT) || check(TokenType.BOOLEAN) || check(TokenType.IDENTIFIER)) {
            // VarDecl or MethodDecl?
            // VarDecl: Type ID ; 
            // MethodDecl: Type ID ( ... ) ...
            // We need lookahead. 
            // Actually MiniJava grammar often puts VarDecls before MethodDecls strictly. 
            // Let's assume strict order or use lookahead 2.
            
            // For simplicity in this LL parser, we'll implement a loose check or just assume MethodDecl starts with 'public' in some grammars, 
            // but in MiniJava methods are often 'public Type name'. 
            // Vars are 'Type name;'.
            
            if (match(TokenType.PUBLIC)) {
                // Definitely a method
                methodDeclaration();
            } else {
                // Variable declaration
                // Need to handle type.
                type();
                consume(TokenType.IDENTIFIER, "Expect variable name.");
                consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
            }
        }
        
        consume(TokenType.RIGHT_BRACE, "Expect '}' after class body.");
    }
    
    private void methodDeclaration() {
        // 'public' already consumed if we came from classDeclaration logic above
        // But strict MiniJava says: MethodDecl -> public Type ID ( FormalList ) { VarDecl* Statement* return Exp ; }
        // My logic above consumed 'public'.
        
        type();
        consume(TokenType.IDENTIFIER, "Expect method name.");
        consume(TokenType.LEFT_PAREN, "Expect '('.");
        if (!check(TokenType.RIGHT_PAREN)) {
            formalList();
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')'.");
        consume(TokenType.LEFT_BRACE, "Expect '{'.");

        // VarDecls
        // In full MiniJava, VarDecls come before Statements.
        while (isTypeStart()) {
            // Needs lookahead to distinguish local var from statement (e.g. Identifier usually starts statement or assignment)
            // Type ID;  vs ID = Exp;
            // 'int' 'boolean' are clear. 'ID' is ambiguous (could be Type or Variable assignment).
            // We need LL(2) or semantic info.
            // For now, let's assume if it starts with int/boolean it is var. 
            // If it starts with ID, check valid types.
            if (check(TokenType.INT) || check(TokenType.BOOLEAN)) {
               varDeclaration();
            } else {
               // Complex case: User defined type vs Assignment.
               // MiniJava usually doesn't allow 'MyType x;' inside methods in some definitions, 
               // but typically it does.
               // We will parse as statement if ambiguous for now to keep simple, or peek next token.
                if (check(TokenType.IDENTIFIER)) { // Potential type
                     if (peekNext() == TokenType.IDENTIFIER) {
                         varDeclaration();
                     } else {
                         break; // Statement
                     }
                } else {
                    break;
                }
            }
        }

        while (!check(TokenType.RETURN) && !check(TokenType.RIGHT_BRACE)) {
            statement();
        }

        consume(TokenType.RETURN, "Expect 'return'.");
        expression();
        consume(TokenType.SEMICOLON, "Expect ';'.");
        consume(TokenType.RIGHT_BRACE, "Expect '}'.");
    }

    private void varDeclaration() {
        type();
        consume(TokenType.IDENTIFIER, "Expect variable name.");
        consume(TokenType.SEMICOLON, "Expect ';'.");
    }

    private void formalList() {
        type();
        consume(TokenType.IDENTIFIER, "Expect parameter name.");
        while (match(TokenType.COMMA)) {
            type();
            consume(TokenType.IDENTIFIER, "Expect parameter name.");
        }
    }

    private void type() {
        if (match(TokenType.INT)) {
            if (match(TokenType.LEFT_BRACKET)) {
                consume(TokenType.RIGHT_BRACKET, "Expect ']' for int array.");
            }
        } else if (match(TokenType.BOOLEAN)) {
            // ok
        } else if (match(TokenType.IDENTIFIER)) {
            // ok
        } else {
            error(peek(), "Expect validation type.");
        }
    }
    
    private boolean isTypeStart() {
        return check(TokenType.INT) || check(TokenType.BOOLEAN) || check(TokenType.IDENTIFIER);
    }
    
    private TokenType peekNext() {
        if (current + 1 >= tokens.size()) return TokenType.EOF;
        return tokens.get(current + 1).type;
    }

    private void statement() {
        if (match(TokenType.LEFT_BRACE)) {
            while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
                statement();
            }
            consume(TokenType.RIGHT_BRACE, "Expect '}'.");
        } else if (match(TokenType.IF)) {
            consume(TokenType.LEFT_PAREN, "Expect '('.");
            expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')'.");
            statement();
            if (match(TokenType.ELSE)) {
                statement();
            }
        } else if (match(TokenType.WHILE)) {
            consume(TokenType.LEFT_PAREN, "Expect '('.");
            expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')'.");
            statement();
        } else if (match(TokenType.IDENTIFIER)) {
            String lexeme = previous().lexeme;
             if (lexeme.equals("System")) {
                 if (match(TokenType.DOT)) {
                     if (match(TokenType.IDENTIFIER) && previous().lexeme.equals("out")) {
                         if (match(TokenType.DOT)) {
                             if (match(TokenType.IDENTIFIER) && previous().lexeme.equals("println")) {
                                 consume(TokenType.LEFT_PAREN, "Expect '('.");
                                 expression();
                                 consume(TokenType.RIGHT_PAREN, "Expect ')'.");
                                 consume(TokenType.SEMICOLON, "Expect ';'.");
                                 return;
                             }
                         }
                     }
                     // If we got here, it matched System.IDENTIFIER but not out.println or something.
                     // It is likely an error or weird assignment System.x = ...
                     // Usually MiniJava doesn't have field access on LHS except via this or new.
                     // But let's assume if it fails the println check, it falls through to assignment check?
                     // No, "match" consumes tokens. We can't backtrack easily in this simple parser.
                     // For this simple project, strict System.out.println check is fine.
                     // If we consumed DOT, we can't fall back to assignment easily.
                     // Let's assume valid MiniJava code is correct or error out.
                     throw error(previous(), "Expect 'System.out.println'.");
                 }
             }

            // Assignment or Array Assignment
            // ID = Exp; or ID [ Exp ] = Exp;
            if (match(TokenType.ASSIGN)) {
                expression();
                consume(TokenType.SEMICOLON, "Expect ';'.");
            } else if (match(TokenType.LEFT_BRACKET)) {
                expression();
                consume(TokenType.RIGHT_BRACKET, "Expect ']'.");
                consume(TokenType.ASSIGN, "Expect '='.");
                expression();
                consume(TokenType.SEMICOLON, "Expect ';'.");
            } else {
                 throw error(previous(), "Expect assignment.");
            }
        } else {
             error(peek(), "Expect statement.");
        }
    }

    // Precedence handling via layering
    private void expression() {
        andExpression();
    }
    
    private void andExpression() {
        relationalExpression();
        while (match(TokenType.AND)) {
            relationalExpression();
        }
    }
    
    private void relationalExpression() {
        additiveExpression();
        while (match(TokenType.LESS_THAN)) {
            additiveExpression();
        }
    }
    
    private void additiveExpression() {
        multiplicativeExpression();
        while (match(TokenType.PLUS) || match(TokenType.MINUS)) {
            multiplicativeExpression();
        }
    }
     
    private void multiplicativeExpression() {
        notExpression();
        while (match(TokenType.TIMES)) {
            notExpression(); // NotExpr is unary high precedence
        }
    }

    private void notExpression() {
        if (match(TokenType.NOT)) {
            notExpression();
        } else {
            term();
        }
    }
    
    private void term() {
        if (match(TokenType.INTEGER_LITERAL)) {
            // Literal
        } else if (match(TokenType.TRUE)) {
            // Literal
        } else if (match(TokenType.FALSE)) {
            // Literal
        } else if (match(TokenType.THIS)) {
            // this
        } else if (match(TokenType.NEW)) {
            if (match(TokenType.INT)) {
                consume(TokenType.LEFT_BRACKET, "Expect '['.");
                expression();
                consume(TokenType.RIGHT_BRACKET, "Expect ']'.");
            } else {
                consume(TokenType.IDENTIFIER, "Expect class name.");
                consume(TokenType.LEFT_PAREN, "Expect '('.");
                consume(TokenType.RIGHT_PAREN, "Expect ')'.");
            }
        } else if (match(TokenType.LEFT_PAREN)) {
            expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')'.");
        } else if (match(TokenType.IDENTIFIER)) {
            // ID
        } else {
            throw error(peek(), "Expect expression.");
        }

        // Handle suffixes: .length, .method(), [index]
        while (true) {
            if (match(TokenType.LEFT_BRACKET)) {
                expression();
                consume(TokenType.RIGHT_BRACKET, "Expect ']'.");
            } else if (match(TokenType.DOT)) {
                if (match(TokenType.LENGTH)) {
                    // .length
                } else {
                     consume(TokenType.IDENTIFIER, "Expect method name.");
                     consume(TokenType.LEFT_PAREN, "Expect '('.");
                     if (!check(TokenType.RIGHT_PAREN)) {
                        expression();
                        while (match(TokenType.COMMA)) expression();
                     }
                     consume(TokenType.RIGHT_PAREN, "Expect ')'.");
                }
            } else {
                break;
            }
        }
    }

    private void consume(TokenType type, String message) {
        if (check(type)) {
            advance();
            return;
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
}
