package org.venecuela.visitor;

import org.venecuela.visitor.exceptions.VariableAlreadyDeclaredException;
import org.venecuela.visitor.exceptions.VariableAlreadyInCurrentScopeException;
import org.venecuela.visitor.exceptions.VariableUndeclaredException;
import org.venecuela.visitor.exceptions.WrongTypeException;

import java.util.HashMap;
import java.util.Map;

public class Scope {
    private final Scope parent;
    private final Map<String, Variable> currentBlockVariables;

    public Scope(Scope parent) {
        this.parent = parent;
        this.currentBlockVariables = new HashMap<>();
    }

    public void putVariable(String type, String name, Object value) {
        if (this.containsVariable(name)) {
            throw new VariableAlreadyDeclaredException();
        }
        if (value instanceof Variable) {
            value = ((Variable) value).getValue();
        }

        Variable val = new Variable(type, name, value);
        this.currentBlockVariables.put(name, val);
    }

    public Object generateVariable(Variable oldVariable, Object newValue) {
        String oldType = oldVariable.getType();
        Boolean bool;
        Integer integer;
        String string;
        if (newValue instanceof Variable) {
            newValue = ((Variable) newValue).getValue();
        }
        switch (oldType) {
            case "bolivar" -> {
                try {
                    integer = (Integer) newValue;
                } catch (Exception e) {
                    throw new WrongTypeException();
                }
                return new Variable(oldType, oldVariable.getName(), integer);
            }
            case "cuerda" -> {
                try {
                    string = (String) newValue;
                } catch (Exception e) {
                    throw new WrongTypeException();
                }
                return new Variable(oldType, oldVariable.getName(), string);
            }
            case "boo" -> {
                try {
                    bool = (Boolean) newValue;
                } catch (Exception e) {
                    throw new WrongTypeException();
                }
                return new Variable(oldType, oldVariable.getName(), bool);
            }
        }
        return null;
    }

    public void addVariable(String name, Object value) {
        if (!this.containsVariable(name)) {
            throw new VariableUndeclaredException();
        }

        Scope scopeParent = this;
        do {
            if (scopeParent.containsCurrentScopeVariable(name)) {
                Variable old = scopeParent.getCurrentScopeVariable(name);
                Variable newVariable = (Variable) this.generateVariable(old, value);
                scopeParent.updateCurrentScopeVariable(old.getName(), newVariable);
                return;
            }
        } while ((scopeParent = scopeParent.parent) != null);
    }

    public boolean containsVariable(String name) {
        Scope scopeParent = this;
        do {
            if (scopeParent.currentBlockVariables.containsKey(name)) {
                return true;
            }
        } while ((scopeParent = scopeParent.parent) != null);
        return false;
    }

    public boolean containsCurrentScopeVariable(String name) {
        return this.currentBlockVariables.containsKey(name);
    }

    public void updateCurrentScopeVariable(String name, Variable value) {
        this.currentBlockVariables.put(name, value);
    }

    public Variable getVariable(String name) {
        Scope scopeParent = this;
        do {
            if (scopeParent.containsCurrentScopeVariable(name)) {
                return scopeParent.getCurrentScopeVariable(name);
            }
        } while ((scopeParent = scopeParent.parent) != null);
        return null;
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

    public void putGlobalVariable(String type, String name, Object value) {
        Scope globalScope = this;
        while ((globalScope.parent) != null) {
            globalScope = globalScope.parent;
        }

        globalScope.putVariable(type, name, value);
    }

    public void transferToCurrentScope(String name) {
        if (this.containsCurrentScopeVariable(name)) {
            throw new VariableAlreadyInCurrentScopeException();
        }
        if (!this.containsVariable(name)) {
            throw new VariableUndeclaredException();
        }

        Variable value = this.removeVariable(name);

        this.currentBlockVariables.put(name, value);
    }

}
