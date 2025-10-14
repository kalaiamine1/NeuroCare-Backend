package com.BrainStack.Exception;

/**
 * Exception levée lorsqu'un parent n'est pas trouvé.
 */
public class ParentNotFoundException extends RuntimeException {
    public ParentNotFoundException(String message) {
        super(message);
    }
}

