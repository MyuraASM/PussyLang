package pussylang.ast.expr;
import pussylang.ast.Expr;
import pussylang.ast.ExprVisitor;

public record LiteralExpr(Object value) implements Expr {
    public <R> R accept(ExprVisitor<R> v) { return v.visitLiteral(this); }
}