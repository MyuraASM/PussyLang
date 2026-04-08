package pussylang.ast.expr;
import pussylang.ast.Expr;
import pussylang.ast.ExprVisitor;

public record HexLiteralExpr(long value) implements Expr {
    public <R> R accept(ExprVisitor<R> v) { return v.visitHexLiteral(this); }
}