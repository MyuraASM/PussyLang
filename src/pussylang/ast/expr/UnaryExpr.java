
package pussylang.ast.expr;
import pussylang.ast.Expr;
import pussylang.ast.ExprVisitor;
import pussylang.lexer.Token;

public record UnaryExpr(Token operator, Expr right) implements Expr {
    public <R> R accept(ExprVisitor<R> v) { return v.visitUnary(this); }
}