package model;

public record MedicalRecord(
        int recordId,
        int petId,
        int appointmentId,
        String diagnosis,
        String treatment,
        String notes
) {}