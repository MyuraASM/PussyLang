package pussylang.lexer;

public enum TokenType {
    //single char token
    LEFT_PAREN, RIGHT_PAREN,
    PLUS, MINUS, STAR, SLASH,
    SEMICOLON, COMMA,

    //1 or 2 char tokens
    EQUAL, EQUAL_EQUAL,
    BANG, BANG_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    //literals
    IDENTIFIER,
    NUMBER,
    STRING,

    //keywords
    PRINT,
    VAR,
    IF,
    ELSE,
    WHILE,
    TRUE,
    FALSE,
    NULL,

    //special shit
    EOF,
    ERROR
}