package com.BrainStack.Enums;
public enum AppointmentStatus {
    PENDING("En attente"),
    CONFIRMED("Confirmé"),
    COMPLETED("Effectué"),
    CANCELLED("Annulé"),
    RESCHEDULED("Reprogrammé");

    private final String label;

    AppointmentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}