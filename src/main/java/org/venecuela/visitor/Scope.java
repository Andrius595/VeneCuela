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
            if (scopeParent.containsSymbol(name)) {
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
        Scope scopeParent = this;
        while (scopeParent != null) {
            if (scopeParent.currentBlockSymbols.containsKey(name)) {
                return true;
            }
            scopeParent = scopeParent.parent;
        }
        return false;
    }

    public boolean containsCurrentScopeSymbol(String name) {
        return this.currentBlockSymbols.containsKey(name);
    }

    public Object getSymbol(String name) {
        if (this.currentBlockSymbols.containsKey(name)) {
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

    public Object getCurrentScopeSymbol(String name) {
        return this.currentBlockSymbols.get(name);
    }

    public Object removeSymbol(String name) {
        Scope scopeParent = this;
        while (scopeParent != null) {
            if (scopeParent.currentBlockSymbols.containsKey(name)) {
                return scopeParent.currentBlockSymbols.remove(name);
            }
            scopeParent = scopeParent.parent;
        }
        throw new NullPointerException();
    }

    public void removeCurrentScopeSymbol(String name) {
        this.currentBlockSymbols.remove(name);
    }

    public void addGlobalSymbol(String name, Object value) {
        Scope globalScope = this;
        while ((globalScope.parent) != null) {
            globalScope = globalScope.parent;
        }

        globalScope.addSymbol(name, value);
    }

    public void transferToCurrentScope(String name) {
        if (this.containsCurrentScopeSymbol(name)) {
            throw new RuntimeException();
        }
        if (!this.containsSymbol(name)) {
            throw new NullPointerException();
        }

        Object value = this.removeSymbol(name);
        this.currentBlockSymbols.put(name, value);
    }

}
