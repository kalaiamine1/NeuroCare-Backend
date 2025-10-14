package com.BrainStack.Exception;

/**
 * Exception levée lorsqu'un enfant n'est pas trouvé.
 */
public class ChildNotFoundException extends RuntimeException {
    public ChildNotFoundException(String message) {
        super(message);
    }
}

