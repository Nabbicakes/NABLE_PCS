package model;

public record Appointment(
        int appointmentId,
        int petId,
        int veterinarianId,
        String appointmentDate,
        String appointmentTime,
        String reason,
        String status
) {
}