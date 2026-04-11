package pussylang.compiler;

import java.util.ArrayList;
import java.util.List;

public class Chunk {

    public final String name;
    public final int    arity;
    public int upvalueCount = 0;

    private final List<Byte>    code      = new ArrayList<>();
    public  final List<Object>  constants = new ArrayList<>();
    private final List<Integer> lines     = new ArrayList<>();

    private byte[] cachedCode;

    public Chunk(String name, int arity) {
        this.name  = name;
        this.arity = arity;
    }

    public void write(byte b, int line) {
        code.add(b);
        lines.add(line);
        cachedCode = null;
    }

    public int addConstant(Object value) {
        constants.add(value);
        return constants.size() - 1;
    }

    public void patchJump(int offset) {
        int jump = code.size() - offset - 2;
        if (jump > 0xFFFF) throw new CompileError("Jump offset too large.");
        code.set(offset,     (byte) ((jump >> 8) & 0xFF));
        code.set(offset + 1, (byte)  (jump        & 0xFF));
        cachedCode = null;
    }

    public int currentOffset() { return code.size(); }
    public int lineAt(int idx) { return lines.get(idx); }


    public byte[] code() {
        if (cachedCode == null) {
            cachedCode = new byte[code.size()];
            for (int i = 0; i < code.size(); i++) cachedCode[i] = code.get(i);
        }
        return cachedCode;
    }


    public List<Object> constants() {
        return constants;
    }
}