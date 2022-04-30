package org.venecuela.visitor;

import java.util.HashMap;
import java.util.Map;

public class Scope {
    private final Scope parent;
    private final Map<String, Object> currentBlockSymbols;

    public Scope(Scope parent) {
        this.parent = parent;
        this.currentBlockSymbols = new HashMap<>();
    }

    public void addSymbol(String name, Object value) {
        if (currentBlockSymbols.containsKey(name)) {
            this.currentBlockSymbols.put(name, value);
            return;
        }

        boolean exists = false;
        Scope scopeParent = this;
        while ((scopeParent = scopeParent.parent) != null) {
            if (scopeParent.containsSymbol(name)){
                exists = true;
                break;
            }
        }
        if (exists) {
            scopeParent.addSymbol(name, value);
        } else {
            this.currentBlockSymbols.put(name, value);
        }

    }

    public boolean containsSymbol(String name) {
        return this.currentBlockSymbols.containsKey(name);
    }

    public Object getSymbol(String name) {
        if (this.currentBlockSymbols.containsKey(name)){
            return this.currentBlockSymbols.get(name);
        } else {
            Scope scopeParent = this;
            while ((scopeParent = scopeParent.parent) != null) {
                if (scopeParent.containsSymbol(name)) {
                    return scopeParent.getSymbol(name);
                }
            }
            return null;
        }
    }

}
