package pussylang.ast.stmt;
import pussylang.ast.*;
import pussylang.lexer.Token;

public record ReturnStmt(Token keyword, Expr value) implements Stmt {
    public <R> R accept(StmtVisitor<R> v) { return v.visitReturn(this); }
}