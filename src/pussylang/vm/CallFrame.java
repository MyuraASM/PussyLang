package pussylang.vm;

import pussylang.compiler.Chunk;

/**
 * one activation record on the VM call stack
 *
 * ip   -> instruction pointer into this frames bytecode snapshot.
 * base -> index into the value stack where this frames locals start.
 *        stack[base + 0] = first param / first local.
 */
public class CallFrame {

    public final  Chunk  chunk;
    private final byte[] code;
    public        int    ip   = 0;
    public final  int    base;

    public CallFrame(Chunk chunk, int base) {
        this.chunk = chunk;
        this.code  = chunk.toByteArray();
        this.base  = base;
    }



    public int    readByte()  { return code[ip++] & 0xFF; }
    public int    readShort() { int hi = readByte(); return (hi << 8) | readByte(); }
    public Object readConst() { return chunk.constants.get(readByte()); }
}