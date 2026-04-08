package pussylang.ast.stmt;
import pussylang.ast.*;
import pussylang.lexer.Token;
import java.util.List;

public record FuncStmt(Token name, List<Token> params, List<Stmt> body) implements Stmt {
    public <R> R accept(StmtVisitor<R> v) { return v.visitFunc(this); }
}