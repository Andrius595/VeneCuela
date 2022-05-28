package org.venecuela.visitor;

import java.util.HashMap;
import java.util.Map;

public class Scope {
    private final Scope parent;
    private final Map<String, Variable> currentBlockVariables;

    public Scope(Scope parent) {
        this.parent = parent;
        this.currentBlockVariables = new HashMap<>();
    }

    public void addVariable(String type, String name, Object value) {
        if (currentBlockVariables.containsKey(name)) {
            Variable val = new Variable(type, name, value);
            this.currentBlockVariables.put(name, val);
            return;
        }

        boolean exists = false;
        Scope scopeParent = this;
        while ((scopeParent = scopeParent.parent) != null) {
            if (scopeParent.containsVariable(name)) {
                exists = true;
                break;
            }
        }
        if (exists) {
            scopeParent.addVariable(type, name, value);
        } else {
            Variable val = new Variable(type, name, value);
            this.currentBlockVariables.put(name, val);
        }
    }

    public boolean containsVariable(String name) {
        Scope scopeParent = this;
        while (scopeParent != null) {
            if (scopeParent.currentBlockVariables.containsKey(name)) {
                return true;
            }
            scopeParent = scopeParent.parent;
        }
        return false;
    }

    public boolean containsCurrentScopeVariable(String name) {
        return this.currentBlockVariables.containsKey(name);
    }

    public Variable getSymbol(String name) {
        if (this.currentBlockVariables.containsKey(name)) {
            return this.currentBlockVariables.get(name);
        } else {
            Scope scopeParent = this;
            while ((scopeParent = scopeParent.parent) != null) {
                if (scopeParent.containsVariable(name)) {
                    return scopeParent.getSymbol(name);
                }
            }
            return null;
        }
    }

    public Variable getCurrentScopeVariable(String name) {
        return this.currentBlockVariables.get(name);
    }

    public Variable removeVariable(String name) {
        Scope scopeParent = this;
        while (scopeParent != null) {
            if (scopeParent.currentBlockVariables.containsKey(name)) {
                return scopeParent.currentBlockVariables.remove(name);
            }
            scopeParent = scopeParent.parent;
        }
        throw new NullPointerException();
    }

    public void removeCurrentScopeVariable(String name) {
        this.currentBlockVariables.remove(name);
    }

    public void addGlobalVariable(String type, String name, Object value) {
        Scope globalScope = this;
        while ((globalScope.parent) != null) {
            globalScope = globalScope.parent;
        }

        globalScope.addVariable(type, name, value);
    }

    public void transferToCurrentScope(String name) {
        if (this.containsCurrentScopeVariable(name)) {
            throw new RuntimeException();
        }
        if (!this.containsVariable(name)) {
            throw new NullPointerException();
        }

        Variable value = this.removeVariable(name);

        this.currentBlockVariables.put(name, value);
    }

}
