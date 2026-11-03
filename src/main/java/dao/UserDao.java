
package dao;

import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserDao {

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM Users WHERE username = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public boolean registerUser(User user) throws SQLException {
        String sql = """
                INSERT INTO Users (username, password, role, status)
                VALUES (?, ?, ?, ?)
                """;

        String hashedPassword = PasswordUtil.hashPassword(user.password());

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.username());
            statement.setString(2, hashedPassword);
            statement.setString(3, user.role());
            statement.setString(4, user.status());

            return statement.executeUpdate() == 1;
        }
    }

    public boolean createStaffAccount(String username, String password)
            throws SQLException {

        if (usernameExists(username)) {
            return false;
        }

        User staff = new User(
                0, username, password, "Staff", "Active"
        );

        return registerUser(staff);
    }

    public User loginUser(String username, String password)
            throws SQLException {

        String sql = "SELECT * FROM Users WHERE username = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    String hashedPassword = result.getString("password");
                    String status = result.getString("status");

                    if (PasswordUtil.checkPassword(password, hashedPassword)
                            && "Active".equalsIgnoreCase(status)) {

                        return new User(
                                result.getInt("user_id"),
                                result.getString("username"),
                                hashedPassword,
                                result.getString("role"),
                                status
                        );
                    }
                }
            }
        }

        return null;
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();

        String sql = """
                SELECT user_id, username, password, role, status
                FROM Users
                ORDER BY user_id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                users.add(new User(
                        result.getInt("user_id"),
                        result.getString("username"),
                        result.getString("password"),
                        result.getString("role"),
                        result.getString("status")
                ));
            }
        }

        return users;
    }

    public List<User> getInactiveUsers() throws SQLException {
        List<User> users = new ArrayList<>();

        String sql = """
                SELECT user_id, username, password, role, status
                FROM Users
                WHERE LOWER(status) = 'inactive'
                ORDER BY user_id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                users.add(new User(
                        result.getInt("user_id"),
                        result.getString("username"),
                        result.getString("password"),
                        result.getString("role"),
                        result.getString("status")
                ));
            }
        }

        return users;
    }

    public boolean updateUserRoleAndStatus(
            int userId, String newRole, String newStatus)
            throws SQLException {

        String role = normalizeRole(newRole);
        String status = normalizeStatus(newStatus);

        if (role == null || status == null) {
            return false;
        }

        String sql = """
                UPDATE Users
                SET role = ?, status = ?
                WHERE user_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, role);
            statement.setString(2, status);
            statement.setInt(3, userId);

            return statement.executeUpdate() == 1;
        }
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return null;
        }

        return switch (role.trim().toLowerCase(Locale.ROOT)) {
            case "admin" -> "Admin";
            case "staff" -> "Staff";
            case "pet owner" -> "Pet Owner";
            default -> null;
        };
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return null;
        }

        return switch (status.trim().toLowerCase(Locale.ROOT)) {
            case "active" -> "Active";
            case "inactive" -> "Inactive";
            default -> null;
        };
    }

    public boolean createTemporaryAdmin() throws SQLException {
        String username = "admin";

        if (usernameExists(username)) {
            String updateSql = """
                    UPDATE Users
                    SET status = ?, role = ?
                    WHERE username = ?
                    """;

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(updateSql)) {

                statement.setString(1, "Active");
                statement.setString(2, "Admin");
                statement.setString(3, username);

                return statement.executeUpdate() > 0;
            }

        } else {
            String insertSql = """
                    INSERT INTO Users (username, password, role, status)
                    VALUES (?, ?, ?, ?)
                    """;

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(insertSql)) {

                statement.setString(1, username);
                statement.setString(2, PasswordUtil.hashPassword("12345"));
                statement.setString(3, "Admin");
                statement.setString(4, "Active");

                return statement.executeUpdate() == 1;
            }
        }
    }
}
