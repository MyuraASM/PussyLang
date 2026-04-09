package pussylang.interpreter;

import java.util.HashMap;
import java.util.Map;

/**
 * one scope level & chains to a parent for variable lookup
 * inner scopes shadow outer ones automatically too.
 */
public class Environment {

    private final Environment parent;
    private final Map<String, Object> values = new HashMap<>();

    /** global scope */
    public Environment() {
        this.parent = null;
    }

    /** local (scope of the block itself) */
    public Environment(Environment parent) {
        this.parent = parent;
    }



    public void define(String name, Object value) {
        values.put(name, value);
    }



    public Object get(String name) {
        if (values.containsKey(name)) return values.get(name);
        if (parent != null) return parent.get(name);
        throw new RuntimeError("Undefined variable '" + name + "'.");
    }



    public void assign(String name, Object value) {
        if (values.containsKey(name)) { values.put(name, value); return; }
        if (parent != null) { parent.assign(name, value); return; }
        throw new RuntimeError("Undefined variable '" + name + "'.");
    }

    //Helper

    public boolean has(String name) {
        if (values.containsKey(name)) return true;
        return parent != null && parent.has(name);
    }
}