
package dao;

import model.Appointment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppointmentDao {

    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("hh:mm a");

    private static final LocalTime[] TIME_SLOTS = {
            LocalTime.of(9, 0),
            LocalTime.of(10, 0),
            LocalTime.of(11, 0),
            LocalTime.of(13, 0),
            LocalTime.of(14, 0),
            LocalTime.of(15, 0),
            LocalTime.of(16, 0)
    };

    // Get registered pets
    public List<String[]> getAllPets() throws SQLException {
        List<String[]> pets = new ArrayList<>();

        String sql = """
            SELECT pet_id, pet_name, species
            FROM Pets
            ORDER BY pet_name
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                pets.add(new String[]{
                        String.valueOf(resultSet.getInt("pet_id")),
                        resultSet.getString("pet_name"),
                        resultSet.getString("species")
                });
            }
        }

        return pets;
    }

    // Get veterinarians
    public List<String[]> getAllVeterinarians() throws SQLException {
        List<String[]> veterinarians = new ArrayList<>();

        String sql = """
            SELECT veterinarian_id, name, specialization,
                   availability, status, working_days
            FROM Veterinarians
            ORDER BY name
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                veterinarians.add(new String[]{
                        String.valueOf(resultSet.getInt("veterinarian_id")),
                        resultSet.getString("name"),
                        resultSet.getString("specialization"),
                        resultSet.getString("availability"),
                        resultSet.getString("status"),
                        resultSet.getString("working_days")
                });
            }
        }

        return veterinarians;
    }

    // Check whether veterinarian is available and active
    private boolean isVeterinarianAvailable(int veterinarianId)
            throws SQLException {

        String sql = """
            SELECT availability, status
            FROM Veterinarians
            WHERE veterinarian_id = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, veterinarianId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return false;
                }

                return "Available".equalsIgnoreCase(
                        resultSet.getString("availability"))
                        && "Active".equalsIgnoreCase(
                        resultSet.getString("status"));
            }
        }
    }

    // Check veterinarian's working day
    private boolean isWorkingDay(
            String workingDays,
            DayOfWeek selectedDay
    ) {

        if (workingDays == null || workingDays.isBlank()) {
            return false;
        }

        for (String day : workingDays.split(",")) {
            if (day.trim().equalsIgnoreCase(selectedDay.toString())) {
                return true;
            }
        }

        return false;
    }

    // Get available dates for the next 30 days
    public List<LocalDate> getAvailableDates(int veterinarianId)
            throws SQLException {

        List<LocalDate> dates = new ArrayList<>();

        String sql = """
            SELECT working_days
            FROM Veterinarians
            WHERE veterinarian_id = ?
              AND LOWER(availability) = 'available'
              AND LOWER(status) = 'active'
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, veterinarianId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return dates;
                }

                String workingDays = resultSet.getString("working_days");

                if (workingDays == null || workingDays.isBlank()) {
                    return dates;
                }

                LocalDate today = LocalDate.now();

                for (int i = 0; i < 30; i++) {

                    LocalDate date = today.plusDays(i);

                    if (isWorkingDay(workingDays, date.getDayOfWeek())) {
                        dates.add(date);
                    }
                }
            }
        }

        return dates;
    }

    // Get hourly time slots and their booking status
    // Array positions:
    // [0] = Start time (HH:mm)
    // [1] = Display time range
    // [2] = Available or Booked
    public List<String[]> getAvailableTimeSlots(
            int veterinarianId,
            String appointmentDate
    ) throws SQLException {

        List<String[]> slots = new ArrayList<>();

        LocalDate date;

        try {
            date = LocalDate.parse(appointmentDate);
        } catch (DateTimeParseException e) {
            return slots;
        }

        if (date.isBefore(LocalDate.now())) {
            return slots;
        }

        if (!isVeterinarianAvailable(veterinarianId)) {
            return slots;
        }

        String workingDays;

        String vetSql = """
            SELECT working_days
            FROM Veterinarians
            WHERE veterinarian_id = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(vetSql)) {

            statement.setInt(1, veterinarianId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return slots;
                }

                workingDays = resultSet.getString("working_days");
            }
        }

        if (!isWorkingDay(workingDays, date.getDayOfWeek())) {
            return slots;
        }

        // Get booked appointment times
        Set<LocalTime> bookedTimes = new HashSet<>();

        String bookedSql = """
            SELECT appointment_time
            FROM Appointments
            WHERE veterinarian_id = ?
              AND appointment_date = ?
              AND LOWER(status) != 'cancelled'
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(bookedSql)) {

            statement.setInt(1, veterinarianId);
            statement.setString(2, appointmentDate);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    String bookedTime =
                            resultSet.getString("appointment_time");

                    try {
                        bookedTimes.add(LocalTime.parse(bookedTime));
                    } catch (DateTimeParseException ignored) {
                        // Ignore invalid stored time values.
                    }
                }
            }
        }

        // Generate time slots
        for (LocalTime start : TIME_SLOTS) {

            LocalTime end = start.plusHours(1);

            String startTime = start.format(
                    DateTimeFormatter.ofPattern("HH:mm")
            );

            String displayRange =
                    start.format(TIME_FORMAT)
                            + " - "
                            + end.format(TIME_FORMAT);

            boolean booked = bookedTimes.contains(start);

            // Past time slots cannot be booked today
            if (date.equals(LocalDate.now())
                    && !start.isAfter(LocalTime.now())) {
                booked = true;
            }

            slots.add(new String[]{
                    startTime,
                    displayRange,
                    booked ? "Booked" : "Available"
            });
        }

        return slots;
    }

    // Create appointment
    public boolean createAppointment(
            int petId,
            int veterinarianId,
            String appointmentDate,
            String appointmentTime,
            String reason,
            int staffUserId
    ) throws SQLException {

        LocalDate date;
        LocalTime time;

        try {
            date = LocalDate.parse(appointmentDate);
            time = LocalTime.parse(appointmentTime);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date or time.");
            return false;
        }

        if (date.isBefore(LocalDate.now())) {
            System.out.println("Appointment date cannot be in the past.");
            return false;
        }

        if (!isValidTimeSlot(time)) {
            System.out.println("Invalid appointment time slot.");
            return false;
        }

        if (date.equals(LocalDate.now())
                && !time.isAfter(LocalTime.now())) {
            System.out.println("This time slot has already passed.");
            return false;
        }

        if (reason == null || reason.isBlank()) {
            System.out.println("Reason cannot be empty.");
            return false;
        }

        // Check pet
        String petSql = """
            SELECT pet_name
            FROM Pets
            WHERE pet_id = ?
            """;

        String petName;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(petSql)) {

            statement.setInt(1, petId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    System.out.println("Pet not found.");
                    return false;
                }

                petName = resultSet.getString("pet_name");
            }
        }

        // Check veterinarian
        String vetSql = """
            SELECT name, availability, status, working_days
            FROM Veterinarians
            WHERE veterinarian_id = ?
            """;

        String veterinarianName;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(vetSql)) {

            statement.setInt(1, veterinarianId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    System.out.println("Veterinarian not found.");
                    return false;
                }

                veterinarianName = resultSet.getString("name");

                if (!"Available".equalsIgnoreCase(
                        resultSet.getString("availability"))) {
                    System.out.println(
                            "This veterinarian is currently unavailable."
                    );
                    return false;
                }

                if (!"Active".equalsIgnoreCase(
                        resultSet.getString("status"))) {
                    System.out.println(
                            "This veterinarian is not active."
                    );
                    return false;
                }

                if (!isWorkingDay(
                        resultSet.getString("working_days"),
                        date.getDayOfWeek())) {

                    System.out.println(
                            "This veterinarian does not work on "
                                    + date.getDayOfWeek() + "."
                    );
                    return false;
                }
            }
        }

        // Check whether the selected slot is still available
        List<String[]> slots = getAvailableTimeSlots(
                veterinarianId,
                appointmentDate
        );

        boolean available = false;

        for (String[] slot : slots) {
            if (slot[0].equals(time.format(
                    DateTimeFormatter.ofPattern("HH:mm")))) {
                available = slot[2].equals("Available");
                break;
            }
        }

        if (!available) {
            System.out.println("This time slot is unavailable.");
            return false;
        }

        // Save appointment
        String insertSql = """
            INSERT INTO Appointments
            (pet_id, veterinarian_id, appointment_date,
             appointment_time, reason, status)
            VALUES (?, ?, ?, ?, ?, 'Scheduled')
            """;

        int appointmentId;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     insertSql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, petId);
            statement.setInt(2, veterinarianId);
            statement.setString(3, appointmentDate);
            statement.setString(
                    4,
                    time.format(DateTimeFormatter.ofPattern("HH:mm"))
            );
            statement.setString(5, reason);

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {

                if (keys.next()) {
                    appointmentId = keys.getInt(1);
                } else {
                    throw new SQLException(
                            "Could not retrieve appointment ID."
                    );
                }
            }
        }

        // Record activity
        try {
            activityLogDao.logAction(
                    staffUserId,
                    "APPOINTMENT_CREATED",
                    "Staff scheduled Appointment #" + appointmentId
                            + " for Pet " + petName
                            + " with Veterinarian " + veterinarianName
                            + " on " + appointmentDate
                            + " at " + appointmentTime + "."
            );

        } catch (SQLException e) {
            System.out.println(
                    "Warning: Appointment was saved, but activity "
                            + "logging failed: " + e.getMessage()
            );
        }

        System.out.println(
                "Appointment created successfully! ID: " + appointmentId
        );

        return true;
    }

    // Validate hourly time slots
    private boolean isValidTimeSlot(LocalTime time) {

        for (LocalTime slot : TIME_SLOTS) {
            if (slot.equals(time)) {
                return true;
            }
        }

        return false;
    }

    // View all appointments
    public List<Appointment> getAllAppointments() throws SQLException {

        List<Appointment> appointments = new ArrayList<>();

        String sql = """
            SELECT appointment_id, pet_id, veterinarian_id,
                   appointment_date, appointment_time, reason, status
            FROM Appointments
            ORDER BY appointment_date, appointment_time
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                appointments.add(new Appointment(
                        resultSet.getInt("appointment_id"),
                        resultSet.getInt("pet_id"),
                        resultSet.getInt("veterinarian_id"),
                        resultSet.getString("appointment_date"),
                        resultSet.getString("appointment_time"),
                        resultSet.getString("reason"),
                        resultSet.getString("status")
                ));
            }
        }

        return appointments;
    }

    // Update appointment status
    public boolean updateAppointmentStatus(
            int appointmentId,
            String newStatus,
            int staffUserId
    ) throws SQLException {

        if (!newStatus.equalsIgnoreCase("Scheduled")
                && !newStatus.equalsIgnoreCase("Completed")
                && !newStatus.equalsIgnoreCase("Cancelled")) {

            System.out.println("Invalid appointment status.");
            return false;
        }

        String oldStatus;

        String sql = """
            SELECT status
            FROM Appointments
            WHERE appointment_id = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, appointmentId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    System.out.println("Appointment not found.");
                    return false;
                }

                oldStatus = resultSet.getString("status");
            }
        }

        String updateSql = """
            UPDATE Appointments
            SET status = ?
            WHERE appointment_id = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(updateSql)) {

            statement.setString(1, newStatus);
            statement.setInt(2, appointmentId);

            statement.executeUpdate();
        }

        try {
            activityLogDao.logAction(
                    staffUserId,
                    "APPOINTMENT_STATUS_UPDATED",
                    "Staff updated Appointment #" + appointmentId
                            + " status from " + oldStatus
                            + " to " + newStatus + "."
            );

        } catch (SQLException e) {
            System.out.println(
                    "Warning: Appointment status was updated, but "
                            + "activity logging failed: " + e.getMessage()
            );
        }

        System.out.println("Appointment status updated successfully!");

        return true;
    }
}
