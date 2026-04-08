package pussylang.lexer;

public class LexerException extends RuntimeException {
    public LexerException(String message, int line) {
        super("[line " + line + "] Lexingg error: " + message);
    }
}