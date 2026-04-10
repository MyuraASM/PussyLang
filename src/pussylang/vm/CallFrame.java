package pussylang.vm;

import pussylang.compiler.Chunk;

public class CallFrame {
    final PussyFunction function;
    final Chunk chunk;
    int ip;
    final int base;


    public CallFrame(PussyFunction function, int base) {
        this.function = function;
        this.chunk = function.chunk();
        this.ip = 0;
        this.base = base;
    }


    public CallFrame(Chunk chunk, int base) {
        this.function = null;
        this.chunk = chunk;
        this.ip = 0;
        this.base = base;
    }

    public byte[] code() {
        return chunk.code();
    }

    public int readByte() {
        return chunk.code()[ip++] & 0xFF;
    }

    public int readShort() {
        return ((readByte() << 8) | readByte());
    }

    public Object readConst() {
        return chunk.constants().get(readByte());
    }
}