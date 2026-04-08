package pussylang.ast.stmt;
import pussylang.ast.*;

public record IfStmt(Expr condition, Stmt thenBranch, Stmt elseBranch) implements Stmt {
    public <R> R accept(StmtVisitor<R> v) { return v.visitIf(this); }
}