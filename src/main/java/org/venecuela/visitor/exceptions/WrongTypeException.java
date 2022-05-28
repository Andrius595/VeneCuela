package org.venecuela.visitor.exceptions;

public class WrongTypeException extends RuntimeException {
    public WrongTypeException() {
        super("Variable type is not supported for this operation");
    }
}
