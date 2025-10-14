package com.BrainStack.Exception;

/**
 * Exception levée lorsqu'un enregistrement de santé (HealthRecord) est introuvable.
 */
public class HealthRecordNotFoundException extends RuntimeException {
    public HealthRecordNotFoundException(String message) {
        super(message);
    }
}

