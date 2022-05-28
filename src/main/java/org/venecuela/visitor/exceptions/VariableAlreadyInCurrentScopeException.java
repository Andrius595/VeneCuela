package org.venecuela.visitor.exceptions;

public class VariableAlreadyInCurrentScopeException extends RuntimeException {
    public VariableAlreadyInCurrentScopeException() {
        super("Variable is already in current scope!");
    }
}
