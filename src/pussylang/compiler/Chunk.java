package pussylang.compiler;

import java.util.ArrayList;
import java.util.List;

/**
 * one compiled unit like a script or a function body.
 * it holds bytecode, constant pool, and a parallel line table for error reporting.
 */
public class Chunk {

    public final String name;
    public final int    arity;

    private final List<Byte>    code      = new ArrayList<>();
    public  final List<Object>  constants = new ArrayList<>();
    private final List<Integer> lines     = new ArrayList<>();

    public Chunk(String name, int arity) {
        this.name  = name;
        this.arity = arity;
    }



    public void write(byte b, int line) {
        code.add(b);
        lines.add(line);
    }



    public int addConstant(Object value) {
        constants.add(value);
        return constants.size() - 1;
    }



    /**
     *  a 2byte big endian jump offset at [offset, offset+1].
     *  call this once the jump target is known.
     */
    public void patchJump(int offset) {
        int jump = code.size() - offset - 2;
        if (jump > 0xFFFF) throw new CompileError("Jump offset too large.");
        code.set(offset,     (byte) ((jump >> 8) & 0xFF));
        code.set(offset + 1, (byte)  (jump        & 0xFF));
    }



    public int    currentOffset()  { return code.size(); }
    public int    lineAt(int idx)  { return lines.get(idx); }

    /** snapshot bytecode as a plain array  used by CallFrame for the fast reads. */
    public byte[] toByteArray() {
        byte[] arr = new byte[code.size()];
        for (int i = 0; i < code.size(); i++) arr[i] = code.get(i);
        return arr;
    }
}