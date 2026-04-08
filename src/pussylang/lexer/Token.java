package pussylang.lexer;

public record Token(
        TokenType type,
        String lexeme,      // this is the actual text
        Object literal,     // for numbers or strings rest null
        int line
) {

    @Override
    public String toString() {
        return type + " '" + lexeme + "' " + (literal != null ? literal : "");
    }
}