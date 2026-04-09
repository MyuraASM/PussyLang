package pussylang.compiler;

import pussylang.ast.Expr;
import pussylang.ast.ExprVisitor;
import pussylang.ast.Stmt;
import pussylang.ast.StmtVisitor;
import pussylang.ast.expr.*;
import pussylang.ast.stmt.*;
import pussylang.lexer.Token;
import pussylang.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

import static pussylang.compiler.OpCode.*;

/**
 *  AST ->> bytecode compiler.
 *
 * one Compiler instance script or function.
 * nested functions spawn a fresh Compiler so each gets its own Chunk.
 *
 * stack layout during a function call:
 *   [ ..., callee, arg0, arg1, arg2, local0, local1, ... ]
 *                  ^ frame.base
 * GET_LOCAL 0 -> arg0,  GET_LOCAL 1 -> </></> arg1,  etc.
 */
public class Compiler implements ExprVisitor<Void>, StmtVisitor<Void> {



    private record Local(String name, int depth) {}

    private final List<Local> locals     = new ArrayList<>();
    private       int         scopeDepth = 0;



    private Chunk chunk;
    private int   currentLine = 1;



    /** Compile a toplevel script into a Chunk. */
    public Chunk compileScript(List<Stmt> stmts) {
        chunk = new Chunk("<script>", 0);
        for (Stmt s : stmts) if (s != null) compileStmt(s);
        emit(HALT);
        return chunk;
    }

    /** Compile a function body into its own Chunk  it is called by visitFunc. */
    Chunk compileFunctionBody(FuncStmt func) {
        chunk      = new Chunk(func.name().lexeme(), func.params().size());
        scopeDepth = 1;
        locals.clear();

        // Params become like slot 0, 1, 2, etc etc
        for (Token p : func.params()) {
            locals.add(new Local(p.lexeme(), 1));
        }

        for (Stmt s : func.body()) if (s != null) compileStmt(s);


        emit(PUSH_NULL);
        emit(RETURN);
        return chunk;
    }



    private void compileStmt(Stmt s) { s.accept(this); }

    @Override
    public Void visitExpr(ExprStmt s) {
        compileExpr(s.expression());
        emit(POP);
        return null;
    }

    @Override
    public Void visitPrint(PrintStmt s) {
        compileExpr(s.expression());
        emit(PRINT);
        return null;
    }

    @Override
    public Void visitVar(VarStmt s) {
        currentLine = s.name().line();

        if (s.initializer() != null) compileExpr(s.initializer());
        else                         emit(PUSH_NULL);

        if (scopeDepth == 0) {
            // pop value and store in global table
            emitWithByte(DEFINE_GLOBAL, addConstant(s.name().lexeme()));
        } else {
            //value stays on stack  that stack slot IS the variable
            locals.add(new Local(s.name().lexeme(), scopeDepth));
        }
        return null;
    }

    @Override
    public Void visitBlock(BlockStmt s) {
        beginScope();
        for (Stmt inner : s.statements()) if (inner != null) compileStmt(inner);
        endScope();
        return null;
    }

    @Override
    public Void visitIf(IfStmt s) {
        compileExpr(s.condition());

        //  condition
        //  JUMP_IF_FALSE -> [else]
        //  POP            (truthy discard condition)
        //  <then branch>
        //  JUMP -> [end]
        // [else]:
        //  POP            (falsy discard condition)
        //  <else branch>
        // [end]:

        int thenJump = emitJump(JUMP_IF_FALSE);
        emit(POP);
        compileStmt(s.thenBranch());

        int elseJump = emitJump(JUMP);
        patchJump(thenJump);
        emit(POP);

        if (s.elseBranch() != null) compileStmt(s.elseBranch());
        patchJump(elseJump);
        return null;
    }

    @Override
    public Void visitWhile(WhileStmt s) {
        int loopStart = chunk.currentOffset();

        compileExpr(s.condition());
        int exitJump = emitJump(JUMP_IF_FALSE);
        emit(POP);

        compileStmt(s.body());
        emitLoop(loopStart);

        patchJump(exitJump);
        emit(POP);
        return null;
    }

    @Override
    public Void visitFunc(FuncStmt s) {
        currentLine = s.name().line();


        Chunk funcChunk = new Compiler().compileFunctionBody(s);
        emitWithByte(CLOSURE, addConstant(funcChunk));

        if (scopeDepth == 0) {
            emitWithByte(DEFINE_GLOBAL, addConstant(s.name().lexeme()));
        } else {
            locals.add(new Local(s.name().lexeme(), scopeDepth));
        }
        return null;
    }

    @Override
    public Void visitReturn(ReturnStmt s) {
        currentLine = s.keyword().line();
        if (s.value() != null) compileExpr(s.value());
        else                   emit(PUSH_NULL);
        emit(RETURN);
        return null;
    }



    private void compileExpr(Expr e) { e.accept(this); }

    @Override
    public Void visitLiteral(LiteralExpr e) {
        Object v = e.value();
        if      (v == null)         emit(PUSH_NULL);
        else if (v.equals(true))    emit(PUSH_TRUE);
        else if (v.equals(false))   emit(PUSH_FALSE);
        else                        emitWithByte(PUSH_CONST, addConstant(v));
        return null;
    }

    @Override
    public Void visitHexLiteral(HexLiteralExpr e) {
        emitWithByte(PUSH_CONST, addConstant((double) e.value()));
        return null;
    }

    @Override
    public Void visitGrouping(GroupingExpr e) {
        compileExpr(e.expression());
        return null;
    }

    @Override
    public Void visitVariable(VariableExpr e) {
        currentLine = e.name().line();
        int slot = resolveLocal(e.name().lexeme());
        if (slot >= 0) emitWithByte(GET_LOCAL,  slot);
        else           emitWithByte(GET_GLOBAL,  addConstant(e.name().lexeme()));
        return null;
    }

    @Override
    public Void visitAssign(AssignExpr e) {
        currentLine = e.name().line();
        compileExpr(e.value());
        int slot = resolveLocal(e.name().lexeme());
        if (slot >= 0) emitWithByte(SET_LOCAL,  slot);
        else           emitWithByte(SET_GLOBAL,  addConstant(e.name().lexeme()));
        return null;
    }

    @Override
    public Void visitUnary(UnaryExpr e) {
        currentLine = e.operator().line();
        compileExpr(e.right());
        switch (e.operator().type()) {
            case MINUS -> emit(NEGATE);
            case BANG  -> emit(NOT);
            default    -> throw new CompileError("Unknown unary op: " + e.operator().type());
        }
        return null;
    }

    @Override
    public Void visitBinary(BinaryExpr e) {
        currentLine = e.operator().line();
        TokenType op = e.operator().type();

        // &&  and  ||  short circuit  issue
        if (op == TokenType.AND_AND) { compileAnd(e); return null; }
        if (op == TokenType.OR_OR)   { compileOr(e);  return null; }

        compileExpr(e.left());
        compileExpr(e.right());

        switch (op) {
            case PLUS          -> emit(ADD);
            case MINUS         -> emit(SUB);
            case STAR          -> emit(MUL);
            case SLASH         -> emit(DIV);
            case PERCENT       -> emit(MOD);
            case EQUAL_EQUAL   -> emit(EQUAL);
            case BANG_EQUAL    -> emit(NOT_EQUAL);
            case LESS          -> emit(LESS);
            case LESS_EQUAL    -> emit(LESS_EQ);
            case GREATER       -> emit(GREATER);
            case GREATER_EQUAL -> emit(GREATER_EQ);
            default -> throw new CompileError("Unknown binary op: " + op);
        }
        return null;
    }

    /** a && b   if a is falsy then leave a on stack and skip b */
    private void compileAnd(BinaryExpr e) {
        compileExpr(e.left());
        int skipRight = emitJump(JUMP_IF_FALSE);
        emit(POP);
        compileExpr(e.right());
        patchJump(skipRight);
    }

    /** a || b   if a is truthy then leave a on stack and skip b */
    private void compileOr(BinaryExpr e) {
        compileExpr(e.left());
        int falseJump = emitJump(JUMP_IF_FALSE);
        int endJump   = emitJump(JUMP);
        patchJump(falseJump);
        emit(POP);
        compileExpr(e.right());
        patchJump(endJump);
    }

    @Override
    public Void visitCall(CallExpr e) {
        currentLine = e.paren().line();
        compileExpr(e.callee());
        for (Expr arg : e.args()) compileExpr(arg);
        emitWithByte(CALL, e.args().size());
        return null;
    }



    private void beginScope() { scopeDepth++; }

    private void endScope() {
        scopeDepth--;

        while (!locals.isEmpty() && locals.getLast().depth() > scopeDepth) {
            emit(POP);
            locals.removeLast();
        }
    }

    /** returns stack slot index or -1 if global. */
    private int resolveLocal(String name) {
        for (int i = locals.size() - 1; i >= 0; i--) {
            if (locals.get(i).name().equals(name)) return i;
        }
        return -1;
    }



    private void emit(OpCode op) {
        chunk.write((byte) op.ordinal(), currentLine);
    }


    private void emitWithByte(OpCode op, int operand) {
        if (operand > 0xFF) throw new CompileError("Operand too large (max 255): " + operand);
        emit(op);
        chunk.write((byte) (operand & 0xFF), currentLine);
    }

    private int addConstant(Object v) { return chunk.addConstant(v); }


    private int emitJump(OpCode op) {
        emit(op);
        chunk.write((byte) 0xFF, currentLine); // hi ph
        chunk.write((byte) 0xFF, currentLine); // lo ph
        return chunk.currentOffset() - 2;
    }

    private void patchJump(int offset) { chunk.patchJump(offset); }


    private void emitLoop(int loopStart) {
        emit(LOOP);

        int offset = chunk.currentOffset() - loopStart + 2;
        if (offset > 0xFFFF) throw new CompileError("Loop body too large.");
        chunk.write((byte) ((offset >> 8) & 0xFF), currentLine);
        chunk.write((byte)  (offset        & 0xFF), currentLine);
    }
}