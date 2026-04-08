package pussylang.ast.stmt;
import pussylang.ast.*;
import java.util.List;

public record BlockStmt(List<Stmt> statements) implements Stmt {
    public <R> R accept(StmtVisitor<R> v) { return v.visitBlock(this); }
}