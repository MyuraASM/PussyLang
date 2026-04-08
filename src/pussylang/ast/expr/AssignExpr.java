package pussylang.ast.expr;
import pussylang.ast.Expr;
import pussylang.ast.ExprVisitor;
import pussylang.lexer.Token;

public record AssignExpr(Token name, Expr value) implements Expr {
    public <R> R accept(ExprVisitor<R> v) { return v.visitAssign(this); }
}