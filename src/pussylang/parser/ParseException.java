package pussylang.parser;

import pussylang.lexer.Token;

public class ParseException extends RuntimeException {
    public final Token token;
    public ParseException(Token token, String message) {
        super("[line " + token.line() + "] Parse error at '" + token.lexeme() + "': " + message);
        this.token = token;
    }
}