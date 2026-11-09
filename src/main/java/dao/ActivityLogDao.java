
package dao;

import model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    // Save a new activity log with the next available ID
    public void logAction(int userId, String actionType,
                          String description) throws SQLException {

        String sql = """
            INSERT INTO activity_logs
            (id, user_id, action_type, description)
            VALUES (
                (SELECT COALESCE(MAX(id), 0) + 1 FROM activity_logs),
                ?, ?, ?
            )
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, actionType);
            statement.setString(3, description);

            statement.executeUpdate();
        }
    }

    // Retrieve all activity logs
    public List<ActivityLog> getAllLogs() throws SQLException {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
            SELECT id, user_id, action_type, description, timestamp
            FROM activity_logs
            ORDER BY timestamp DESC, id DESC
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                ActivityLog log = new ActivityLog(
                        resultSet.getInt("id"),
                        resultSet.getInt("user_id"),
                        resultSet.getString("action_type"),
                        resultSet.getString("description"),
                        resultSet.getString("timestamp")
                );

                logs.add(log);
            }
        }

        return logs;
    }

    // Retrieve activity logs by user ID
    public List<ActivityLog> getLogsByUserId(int userId)
            throws SQLException {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
            SELECT id, user_id, action_type, description, timestamp
            FROM activity_logs
            WHERE user_id = ?
            ORDER BY timestamp DESC, id DESC
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    ActivityLog log = new ActivityLog(
                            resultSet.getInt("id"),
                            resultSet.getInt("user_id"),
                            resultSet.getString("action_type"),
                            resultSet.getString("description"),
                            resultSet.getString("timestamp")
                    );

                    logs.add(log);
                }
            }
        }

        return logs;
    }

    // Delete all existing activity logs
    public void clearAllLogs() throws SQLException {

        String sql = "DELETE FROM activity_logs";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();
        }
    }
}
