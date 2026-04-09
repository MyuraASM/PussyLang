package pussylang.interpreter;

import pussylang.ast.stmt.FuncStmt;

import java.util.List;

/**
 * a user defined function it captures its closure environment at definition time.
 */
public class PussyFunction implements PussyCallable {

    private final FuncStmt declaration;
    private final Environment closure;

    public PussyFunction(FuncStmt declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public int arity() {
        return declaration.params().size();
    }

    @Override
    public String name() {
        return declaration.name().lexeme();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        // each call gets its own scope and chained to the closure
        Environment local = new Environment(closure);
        for (int i = 0; i < declaration.params().size(); i++) {
            local.define(declaration.params().get(i).lexeme(), args.get(i));
        }
        try {
            interpreter.executeBlock(declaration.body(), local);
        } catch (ReturnSignal ret) {
            return ret.value;
        }
        return null;
    }

    @Override
    public String toString() {
        return "<func " + declaration.name().lexeme() + ">";
    }
}