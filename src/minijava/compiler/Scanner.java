package minijava.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private boolean hadError = false;
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int lineStart = 0; 

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("class", TokenType.CLASS);
        keywords.put("public", TokenType.PUBLIC);
        keywords.put("static", TokenType.STATIC);
        keywords.put("void", TokenType.VOID);
        keywords.put("main", TokenType.MAIN);
        keywords.put("String", TokenType.STRING);
        keywords.put("extends", TokenType.EXTENDS);
        keywords.put("return", TokenType.RETURN);
        keywords.put("int", TokenType.INT);
        keywords.put("boolean", TokenType.BOOLEAN);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("length", TokenType.LENGTH);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("this", TokenType.THIS);
        keywords.put("new", TokenType.NEW);
    }

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        
        int eofColumn = current - lineStart + 1;
        tokens.add(new Token(TokenType.EOF, "", line, eofColumn));
        return tokens;
    }

    private void scanToken() {
        if (isAtEnd()) return;
        char c = advance();
        switch (c) {
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case '[': addToken(TokenType.LEFT_BRACKET); break;
            case ']': addToken(TokenType.RIGHT_BRACKET); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.TIMES); break;
            case '!': addToken(TokenType.NOT); break;
            case '=': addToken(TokenType.ASSIGN); break;
            case '<': addToken(TokenType.LESS_THAN); break;
            case '&':
                if (match('&')) {
                    addToken(TokenType.AND);
                } else {
                    hadError = true;
                    int errorCol = current - lineStart;
                    System.err.println("[Line " + line + ", Col " + errorCol + "] Unexpected character '&'");
                }
                break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    hadError = true;
                    int errorCol = current - lineStart;
                    System.err.println("[Line " + line + ", Col " + errorCol + "] Unexpected character '/'");
                }
                break;

            case ' ':
            case '\r':
            case '\t':
                break;

            case '\n':
                line++;
                lineStart = current;
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    hadError = true;
                    int errorCol = current - lineStart;
                    System.err.println("[Line " + line + ", Col " + errorCol + "] Unexpected character: " + c);
                }
                break;
        }
    }

    public boolean hadError() {
        return hadError;
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();
        addToken(TokenType.INTEGER_LITERAL);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        String text = source.substring(start, current);
        int column = start - lineStart + 1;
        tokens.add(new Token(type, text, line, column));
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}