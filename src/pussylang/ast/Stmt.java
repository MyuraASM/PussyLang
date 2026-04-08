package pussylang.ast;

public interface Stmt {
    <R> R accept(StmtVisitor<R> visitor);
}