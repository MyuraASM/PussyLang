package pussylang.ast.expr;
import pussylang.ast.Expr;
import pussylang.ast.ExprVisitor;

public record GroupingExpr(Expr expression) implements Expr {
    public <R> R accept(ExprVisitor<R> v) { return v.visitGrouping(this); }
}