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
            if (match(TokenType.PUBLIC)) {
                methodDeclaration();
            } else {
                type();
                consume(TokenType.IDENTIFIER, "Expect variable name.");
                consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
            }
        }
        
        consume(TokenType.RIGHT_BRACE, "Expect '}' after class body.");
    }
    
    private void methodDeclaration() {
        type();
        consume(TokenType.IDENTIFIER, "Expect method name.");
        consume(TokenType.LEFT_PAREN, "Expect '('.");
        if (!check(TokenType.RIGHT_PAREN)) {
            formalList();
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')'.");
        consume(TokenType.LEFT_BRACE, "Expect '{'.");

        while (isVarDeclaration()) {
            varDeclaration();
        }

        while (!check(TokenType.RETURN) && !check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
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
    
    private TokenType peekNext() {
        if (current + 1 >= tokens.size()) return TokenType.EOF;
        return tokens.get(current + 1).type;
    }

    private void statement() {
        try {
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
                         throw error(previous(), "Expect 'System.out.println'.");
                     }
                 }

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
                 throw error(peek(), "Expect statement.");
            }
        } catch (ParseError error) {
            synchronize();
        }
    }

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
            notExpression(); 
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
        } else if (match(TokenType.TRUE)) {
        } else if (match(TokenType.FALSE)) {
        } else if (match(TokenType.THIS)) {
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
        } else {
            throw error(peek(), "Expect expression.");
        }

        while (true) {
            if (match(TokenType.LEFT_BRACKET)) {
                expression();
                consume(TokenType.RIGHT_BRACKET, "Expect ']'.");
            } else if (match(TokenType.DOT)) {
                if (match(TokenType.LENGTH)) {
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