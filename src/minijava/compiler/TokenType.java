package minijava.compiler;

public enum TokenType {
    // Keywords
    CLASS, PUBLIC, STATIC, VOID, MAIN, STRING, EXTENDS, RETURN,
    INT, BOOLEAN, IF, ELSE, WHILE, PRINT, LENGTH, TRUE, FALSE, THIS, NEW,

    // Operators
    PLUS, MINUS, TIMES, AND, LESS_THAN, NOT, EQUALS,

    // Punctuation
    LEFT_PAREN, RIGHT_PAREN,   // ( )
    LEFT_BRACE, RIGHT_BRACE,   // { }
    LEFT_BRACKET, RIGHT_BRACKET, // [ ]
    SEMICOLON, COMMA, DOT, ASSIGN,

    // Literals
    IDENTIFIER, INTEGER_LITERAL,

    // Special
    EOF
}

