package pussylang.vm;

import pussylang.compiler.Chunk;
import pussylang.compiler.OpCode;

import java.util.*;

/**
 * stack based bytecode virtual machine.
 *
 * value stack layout during a call:
 *
 *   index:  0   1   2  ...  base-1  base   base+1  ...
 *           [script locals]  callee  arg0   arg1   ...  <- current frame
 *
 * frame.base points to arg0 (first param / first local of the called function).
 * GET_LOCAL n  -> stack[frame.base + n]
 *
 * On RETURN:
 *   pop result
 *    stackTop = frame.base - 1
 *    push result
 */
public class VM {

    private static final int STACK_MAX  = 1024;
    private static final int FRAMES_MAX = 64;



    private final Object[] stack = new Object[STACK_MAX];
    private  int top   = 0;



    private final CallFrame[] frames = new CallFrame[FRAMES_MAX];
    private  int depth  = 0;



    private final Map<String, Object> globals = new LinkedHashMap<>();

    public VM() {
        NativeRegistry.registerAll(globals::put);
    }



    public void run(Chunk script) {
        pushFrame(new CallFrame(script, 0));
        execute();
    }



    private void execute() {
        CallFrame f = frames[depth - 1];

        loop: for (;;) {
            OpCode op = OpCode.values()[f.readByte()];

            switch (op) {


                case PUSH_CONST  -> push(f.readConst());
                case PUSH_NULL   -> push(null);
                case PUSH_TRUE   -> push(Boolean.TRUE);
                case PUSH_FALSE  -> push(Boolean.FALSE);


                case POP         -> pop();


                case ADD -> {
                    Object b = pop(), a = pop();
                    if (a instanceof Double da && b instanceof Double db)
                        push(da + db);
                    else
                        push(NativeRegistry.stringify(a) + NativeRegistry.stringify(b));
                }
                case SUB    -> { double b = popNum(); push(popNum() - b); }
                case MUL    -> { double b = popNum(); push(popNum() * b); }
                case DIV    -> {
                    double b = popNum();
                    if (b == 0) throw new VMError("Division by zero.");
                    push(popNum() / b);
                }
                case MOD    -> { double b = popNum(); push(popNum() % b); }
                case NEGATE -> push(-popNum());


                case NOT        -> push(!isTruthy(pop()));
                case EQUAL      -> { Object b = pop(); push( isEqual(pop(), b)); }
                case NOT_EQUAL  -> { Object b = pop(); push(!isEqual(pop(), b)); }
                case LESS       -> { double b = popNum(); push(popNum() <  b); }
                case LESS_EQ    -> { double b = popNum(); push(popNum() <= b); }
                case GREATER    -> { double b = popNum(); push(popNum() >  b); }
                case GREATER_EQ -> { double b = popNum(); push(popNum() >= b); }


                case DEFINE_GLOBAL -> globals.put((String) f.readConst(), pop());
                case GET_GLOBAL    -> {
                    String name = (String) f.readConst();
                    if (!globals.containsKey(name))
                        throw new VMError("Undefined variable '" + name + "'.");
                    push(globals.get(name));
                }
                case SET_GLOBAL    -> {
                    String name = (String) f.readConst();
                    if (!globals.containsKey(name))
                        throw new VMError("Undefined variable '" + name + "'.");
                    globals.put(name, peek(0));
                }


                case GET_LOCAL -> push(stack[f.base + f.readByte()]);
                case SET_LOCAL -> stack[f.base + f.readByte()] = peek(0);


                case JUMP          -> f.ip += f.readShort();
                case JUMP_IF_FALSE -> { int off = f.readShort(); if (!isTruthy(peek(0))) f.ip += off; }
                case LOOP          -> f.ip -= f.readShort();


                case CLOSURE -> push(new PussyFunction((Chunk) f.readConst()));

                case CALL -> {
                    int    argc   = f.readByte();
                    Object callee = stack[top - argc - 1];

                    if (callee instanceof PussyFunction fn) {
                        if (fn.chunk().arity != argc)
                            throw new VMError("'" + fn.chunk().name + "' expects "
                                    + fn.chunk().arity + " args, got " + argc + ".");

                        f = pushFrame(new CallFrame(fn.chunk(), top - argc));

                    } else if (callee instanceof NativeFunction fn) {
                        if (fn.arity() != -1 && fn.arity() != argc)
                            throw new VMError("'" + fn.name() + "' expects "
                                    + fn.arity() + " args, got " + argc + ".");
                        List<Object> args = new ArrayList<>(argc);
                        for (int i = top - argc; i < top; i++) args.add(stack[i]);
                        top -= argc + 1;
                        push(fn.call(args));

                    } else {
                        throw new VMError("'" + NativeRegistry.stringify(callee) + "' is not callable.");
                    }
                }

                case RETURN -> {
                    Object result = pop();
                    int    base   = f.base;
                    depth--;
                    if (depth == 0) break loop;

                    top = base - 1;
                    push(result);
                    f = frames[depth - 1];
                }


                case PRINT -> System.out.println(NativeRegistry.stringify(pop()));
                case HALT  -> { break loop; }
            }
        }
    }


    //stack helpers


    private void      push(Object v)       { stack[top++] = v; }
    private Object    pop()                { return stack[--top]; }
    private Object    peek(int dist)       { return stack[top - 1 - dist]; }
    private CallFrame pushFrame(CallFrame f) { frames[depth++] = f; return f; }

    private double popNum() {
        Object v = pop();
        if (v instanceof Double d) return d;
        throw new VMError("Expected a number, got: " + NativeRegistry.stringify(v));
    }

    private boolean isTruthy(Object v) {
        if (v == null)              return false;
        if (v instanceof Boolean b) return b;
        if (v instanceof Double  d) return d != 0;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null) return b == null;
        return a.equals(b);
    }
}