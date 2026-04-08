package pussylang.ast.expr;
import pussylang.ast.Expr;
import pussylang.ast.ExprVisitor;
import pussylang.lexer.Token;
import java.util.List;

public record CallExpr(Expr callee, Token paren, List<Expr> args) implements Expr {
    public <R> R accept(ExprVisitor<R> v) { return v.visitCall(this); }
}