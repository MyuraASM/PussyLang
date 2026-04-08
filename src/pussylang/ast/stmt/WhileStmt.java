package pussylang.ast.stmt;
import pussylang.ast.*;

public record WhileStmt(Expr condition, Stmt body) implements Stmt {
    public <R> R accept(StmtVisitor<R> v) { return v.visitWhile(this); }
}