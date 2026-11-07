package dao;

import model.MedicalRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecordDao {

    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    // Add a medical record
    public void addMedicalRecord(
            int petId,
            int appointmentId,
            String diagnosis,
            String treatment,
            String notes,
            int staffUserId
    ) throws SQLException {

        if (diagnosis == null || diagnosis.isBlank()) {
            throw new SQLException("Diagnosis cannot be empty.");
        }

        if (treatment == null || treatment.isBlank()) {
            throw new SQLException("Treatment cannot be empty.");
        }

        String sql = """
            INSERT INTO Medical_Records
            (pet_id, appointment_id, diagnosis, treatment, notes)
            VALUES (?, ?, ?, ?, ?)
            """;

        int recordId;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql,
                     java.sql.Statement.RETURN_GENERATED_KEYS
             )) {

            statement.setInt(1, petId);
            statement.setInt(2, appointmentId);

            statement.setString(3, diagnosis.trim());
            statement.setString(4, treatment.trim());
            statement.setString(5, notes == null ? "" : notes.trim());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Could not retrieve medical record ID.");
                }

                recordId = keys.getInt(1);
            }
        }

        try {
            activityLogDao.logAction(
                    staffUserId,
                    "MEDICAL_RECORD_CREATED",
                    "Staff created Medical Record #" + recordId
                            + " for Pet #" + petId
                            + " and Appointment #" + appointmentId + "."
            );
        } catch (SQLException e) {
            System.out.println(
                    "Warning: Medical record was saved, but activity logging failed: "
                            + e.getMessage()
            );
        }

        System.out.println("Medical record added successfully!");
    }

    // Get all medical records
    public List<MedicalRecord> getAllMedicalRecords() throws SQLException {

        List<MedicalRecord> records = new ArrayList<>();

        String sql = """
            SELECT record_id, pet_id, appointment_id,
                   diagnosis, treatment, notes
            FROM Medical_Records
            ORDER BY record_id DESC
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                records.add(new MedicalRecord(
                        resultSet.getInt("record_id"),
                        resultSet.getInt("pet_id"),
                        resultSet.getInt("appointment_id"),
                        resultSet.getString("diagnosis"),
                        resultSet.getString("treatment"),
                        resultSet.getString("notes")
                ));
            }
        }

        return records;
    }

    // Get medical records for a specific pet
    public List<MedicalRecord> getMedicalRecordsByPetId(int petId)
            throws SQLException {

        List<MedicalRecord> records = new ArrayList<>();

        String sql = """
            SELECT record_id, pet_id, appointment_id,
                   diagnosis, treatment, notes
            FROM Medical_Records
            WHERE pet_id = ?
            ORDER BY record_id DESC
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, petId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(new MedicalRecord(
                            resultSet.getInt("record_id"),
                            resultSet.getInt("pet_id"),
                            resultSet.getInt("appointment_id"),
                            resultSet.getString("diagnosis"),
                            resultSet.getString("treatment"),
                            resultSet.getString("notes")
                    ));
                }
            }
        }

        return records;
    }
}