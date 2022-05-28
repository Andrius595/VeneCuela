package org.venecuela.visitor.exceptions;

public class VariableUndeclaredException extends RuntimeException{
    public VariableUndeclaredException() {
        super("Variable undeclared!");
    }
}
