package com.BrainStack.Enums;

public enum AppointmentType {
    MEDICAL("Rendez-vous Médical"),
    THERAPEUTIC("Séance Thérapeutique"),
    EDUCATIONAL("Suivi Éducatif"),
    CONSULTATION("Consultation");

    private final String label;

    AppointmentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}