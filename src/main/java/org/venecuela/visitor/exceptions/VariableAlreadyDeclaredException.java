package org.venecuela.visitor.exceptions;

public class VariableAlreadyDeclaredException extends RuntimeException {
    public VariableAlreadyDeclaredException() {
        super("Variable is already declared!");
    }
}
