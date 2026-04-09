package pussylang.interpreter;

import java.util.List;

/**
 * anything that can be called with () so it is user functions AND native builtins.
 */
public interface PussyCallable {
    int arity();                                        // expected arg count (-1 = variadic)
    Object call(Interpreter interpreter, List<Object> args);
    String name();
}