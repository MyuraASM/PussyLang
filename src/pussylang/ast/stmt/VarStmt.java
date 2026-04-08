package pussylang.ast.stmt;
import pussylang.ast.*;
import pussylang.lexer.Token;

public record VarStmt(Token name, Expr initializer) implements Stmt {
    public <R> R accept(StmtVisitor<R> v) { return v.visitVar(this); }
}