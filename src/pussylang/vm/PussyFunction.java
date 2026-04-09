package pussylang.vm;

import pussylang.compiler.Chunk;

/**
 * A 1stC function value inside the VM.
 * Wraps a compiled Chunk no closures yet.
 */
public record PussyFunction(Chunk chunk) {
    @Override
    public String toString() { return "<func " + chunk.name + ">"; }
}