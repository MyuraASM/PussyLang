package pussylang.vm;

import java.util.List;

/** anything callable from PussyLang thats implemented in Java. */
public interface NativeFunction {
    String name();
    int    arity();
    Object call(List<Object> args);
}