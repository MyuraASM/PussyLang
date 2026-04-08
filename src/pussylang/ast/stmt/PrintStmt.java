package pussylang.ast.stmt;
import pussylang.ast.*;

public record PrintStmt(Expr expression) implements Stmt {
    public <R> R accept(StmtVisitor<R> v) { return v.visitPrint(this); }
}