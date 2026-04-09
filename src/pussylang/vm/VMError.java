package pussylang.vm;

public class VMError extends RuntimeException {
    public VMError(String message) { super(message); }
}