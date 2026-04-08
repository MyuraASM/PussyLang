package pussylang.ast;

import pussylang.ast.stmt.*;

public interface StmtVisitor<R> {
    R visitExpr(ExprStmt stmt);
    R visitPrint(PrintStmt stmt);
    R visitVar(VarStmt stmt);
    R visitBlock(BlockStmt stmt);
    R visitIf(IfStmt stmt);
    R visitWhile(WhileStmt stmt);
    R visitFunc(FuncStmt stmt);
    R visitReturn(ReturnStmt stmt);
}