package pussylang.interpreter;

/**
 * not a real real error & thrown to unwind the call stack when
 * a return statement is hit, caught by PussyFunction.
 */
public class ReturnSignal extends RuntimeException {

    public final Object value;

    public ReturnSignal(Object value) {
        super(null, null, true, false); //disable stack traceo verhead
        this.value = value;
    }
}