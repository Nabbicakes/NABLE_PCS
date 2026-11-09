
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PaymentDao {

    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    // Get completed appointments that do not have a payment record
    public List<String[]> getCompletedUnpaidAppointments() throws SQLException {
        List<String[]> appointments = new ArrayList<>();

        String sql = """
            SELECT a.appointment_id,
                   p.pet_name,
                   a.appointment_date,
                   a.appointment_time,
                   a.reason
            FROM Appointments a
            JOIN Pets p ON a.pet_id = p.pet_id
            WHERE LOWER(a.status) = 'completed'
              AND NOT EXISTS (
                  SELECT 1
                  FROM Payments pay
                  WHERE pay.appointment_id = a.appointment_id
              )
            ORDER BY a.appointment_date DESC
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                appointments.add(new String[]{
                        String.valueOf(resultSet.getInt("appointment_id")),
                        resultSet.getString("pet_name"),
                        resultSet.getString("appointment_date"),
                        resultSet.getString("appointment_time"),
                        resultSet.getString("reason")
                });
            }
        }

        return appointments;
    }

    // Record a payment and return its generated Payment ID
    public int recordPayment(
            int appointmentId,
            double amount,
            String paymentStatus,
            int staffUserId
    ) throws SQLException {

        if (amount <= 0) {
            throw new SQLException("Payment amount must be greater than zero.");
        }

        if (!"Paid".equalsIgnoreCase(paymentStatus)
                && !"Unpaid".equalsIgnoreCase(paymentStatus)) {
            throw new SQLException("Payment status must be Paid or Unpaid.");
        }

        String checkSql = """
            SELECT status
            FROM Appointments
            WHERE appointment_id = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(checkSql)) {

            statement.setInt(1, appointmentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Appointment not found.");
                }

                if (!"Completed".equalsIgnoreCase(
                        resultSet.getString("status"))) {
                    throw new SQLException(
                            "Payment can only be recorded for completed appointments."
                    );
                }
            }
        }

        String insertSql = """
            INSERT INTO Payments
            (appointment_id, amount, payment_date, payment_status)
            VALUES (?, ?, ?, ?)
            """;

        int paymentId;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertSql)) {

            statement.setInt(1, appointmentId);
            statement.setDouble(2, amount);
            statement.setString(3, LocalDate.now().toString());
            statement.setString(
                    4,
                    paymentStatus.equalsIgnoreCase("Paid") ? "Paid" : "Unpaid"
            );

            statement.executeUpdate();

            try (PreparedStatement idStatement =
                         connection.prepareStatement("SELECT last_insert_rowid()");
                 ResultSet resultSet = idStatement.executeQuery()) {

                if (!resultSet.next()) {
                    throw new SQLException("Unable to retrieve Payment ID.");
                }

                paymentId = resultSet.getInt(1);
            }
        }

        // Record activity log
        try {
            activityLogDao.logAction(
                    staffUserId,
                    "PAYMENT_RECORDED",
                    "Staff recorded a payment for Appointment #"
                            + appointmentId + ". Amount: " + amount
                            + ", Status: " + paymentStatus + "."
            );
        } catch (SQLException e) {
            System.out.println(
                    "Warning: Payment was saved, but activity logging failed: "
                            + e.getMessage()
            );
        }

        System.out.println("Payment recorded successfully!");

        return paymentId;
    }

    // Retrieve payment details for receipt generation
    public String[] getPaymentReceipt(int paymentId) throws SQLException {

        String sql = """
            SELECT pay.payment_id,
                   pay.appointment_id,
                   p.pet_name,
                   a.appointment_date,
                   a.appointment_time,
                   pay.amount,
                   pay.payment_date,
                   pay.payment_status
            FROM Payments pay
            JOIN Appointments a
                ON pay.appointment_id = a.appointment_id
            JOIN Pets p
                ON a.pet_id = p.pet_id
            WHERE pay.payment_id = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, paymentId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    throw new SQLException("Payment receipt not found.");
                }

                return new String[]{
                        String.valueOf(resultSet.getInt("payment_id")),
                        String.valueOf(resultSet.getInt("appointment_id")),
                        resultSet.getString("pet_name"),
                        resultSet.getString("appointment_date"),
                        resultSet.getString("appointment_time"),
                        String.format("%.2f", resultSet.getDouble("amount")),
                        resultSet.getString("payment_date"),
                        resultSet.getString("payment_status")
                };
            }
        }
    }

    // Get all payment records
    public List<String[]> getAllPayments() throws SQLException {
        List<String[]> payments = new ArrayList<>();

        String sql = """
            SELECT payment_id,
                   appointment_id,
                   amount,
                   payment_date,
                   payment_status
            FROM Payments
            ORDER BY payment_id DESC
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                payments.add(new String[]{
                        String.valueOf(resultSet.getInt("payment_id")),
                        String.valueOf(resultSet.getInt("appointment_id")),
                        String.format("%.2f", resultSet.getDouble("amount")),
                        resultSet.getString("payment_date"),
                        resultSet.getString("payment_status")
                });
            }
        }

        return payments;
    }

    // Update payment status
    public void updatePaymentStatus(
            int paymentId,
            String newStatus,
            int staffUserId
    ) throws SQLException {

        if (!"Paid".equalsIgnoreCase(newStatus)
                && !"Unpaid".equalsIgnoreCase(newStatus)) {
            throw new SQLException("Payment status must be Paid or Unpaid.");
        }

        String sql = """
            UPDATE Payments
            SET payment_status = ?
            WHERE payment_id = ?
            """;

        int rowsUpdated;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    newStatus.equalsIgnoreCase("Paid") ? "Paid" : "Unpaid"
            );
            statement.setInt(2, paymentId);

            rowsUpdated = statement.executeUpdate();
        }

        if (rowsUpdated == 0) {
            throw new SQLException("Payment record not found.");
        }

        try {
            activityLogDao.logAction(
                    staffUserId,
                    "PAYMENT_STATUS_UPDATED",
                    "Staff updated Payment #" + paymentId
                            + " status to " + newStatus + "."
            );
        } catch (SQLException e) {
            System.out.println(
                    "Warning: Payment status was updated, but activity logging failed: "
                            + e.getMessage()
            );
        }

        System.out.println("Payment status updated successfully!");
    }
}
