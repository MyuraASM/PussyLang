package pussylang.compiler;

public class Disassembler {

    public static void disassemble(Chunk chunk) {
        System.out.println("" + chunk.name + " (arity=" + chunk.arity + ") ");
        byte[] code = chunk.code();
        int i = 0;
        while (i < code.length) {
            i = instruction(chunk, code, i);
        }

        for (Object c : chunk.constants) {
            if (c instanceof Chunk nested) {
                System.out.println();
                disassemble(nested);
            }
        }
    }

    private static int instruction(Chunk chunk, byte[] code, int offset) {
        System.out.printf("%04d ", offset);

        int line = chunk.lineAt(offset);
        if (offset > 0 && line == chunk.lineAt(offset - 1))
            System.out.print("   | ");
        else
            System.out.printf("%4d ", line);

        OpCode op = OpCode.values()[code[offset] & 0xFF];

        switch (op) {
            case PUSH_NULL: case PUSH_TRUE: case PUSH_FALSE:
            case POP: case ADD: case SUB: case MUL: case DIV: case MOD:
            case NEGATE: case NOT: case EQUAL: case NOT_EQUAL:
            case LESS: case LESS_EQ: case GREATER: case GREATER_EQ:
            case PRINT: case RETURN: case HALT:
                System.out.println(op);
                return offset + 1;

            case PUSH_CONST: case DEFINE_GLOBAL: case GET_GLOBAL: case SET_GLOBAL: {
                int idx = code[offset + 1] & 0xFF;
                Object val = chunk.constants.get(idx);
                String display = val instanceof Chunk c ? "<func " + c.name + ">" : String.valueOf(val);
                System.out.printf("%-20s  [%d] %s%n", op, idx, display);
                return offset + 2;
            }

            case CLOSURE: {
                int idx = code[offset + 1] & 0xFF;
                Object val = chunk.constants.get(idx);
                String display = val instanceof Chunk c ? "<func " + c.name + ">" : String.valueOf(val);
                System.out.printf("%-20s  [%d] %s", op, idx, display);

                int upvalueCount = (val instanceof Chunk c) ? c.upvalueCount : 0;
                System.out.printf("  upvalues=%d", upvalueCount);
                int pos = offset + 2;
                for (int i = 0; i < upvalueCount; i++) {
                    boolean isLocal = code[pos++] != 0;
                    int index = code[pos++] & 0xFF;
                    System.out.printf(" %s%d", isLocal ? "L" : "U", index);
                }
                System.out.println();
                return pos;
            }

            case GET_LOCAL: case SET_LOCAL: case CALL: {
                int slot = code[offset + 1] & 0xFF;
                System.out.printf("%-20s  slot=%d%n", op, slot);
                return offset + 2;
            }

            case GET_UPVALUE: case SET_UPVALUE: {
                int slot = code[offset + 1] & 0xFF;
                System.out.printf("%-20s  upvalue=%d%n", op, slot);
                return offset + 2;
            }

            case JUMP: case JUMP_IF_FALSE: {
                int jump = ((code[offset + 1] & 0xFF) << 8) | (code[offset + 2] & 0xFF);
                System.out.printf("%-20s  +%d  ->  %04d%n", op, jump, offset + 3 + jump);
                return offset + 3;
            }

            case LOOP: {
                int jump = ((code[offset + 1] & 0xFF) << 8) | (code[offset + 2] & 0xFF);
                System.out.printf("%-20s  -%d  ->  %04d%n", op, jump, offset + 3 - jump);
                return offset + 3;
            }

            default:
                System.out.println(op + " (unhandled)");
                return offset + 1;
        }
    }
}