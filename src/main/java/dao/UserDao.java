
package dao;

import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE username = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    // Register a user with the correct role and status.
    public boolean registerUser(User user) throws SQLException {

        String role = normalizeRole(user.role());
        String status = normalizeStatus(role, user.status());

        if (role == null || status == null) {
            return false;
        }

        // Prevent registration of another Admin account.
        if (role.equals("Admin") && hasAdminAccount()) {
            return false;
        }

        String sql = """
                INSERT INTO Users (username, password, role, status)
                VALUES (?, ?, ?, ?)
                """;

        String hashedPassword = PasswordUtil.hashPassword(user.password());

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.username());
            statement.setString(2, hashedPassword);
            statement.setString(3, role);
            statement.setString(4, status);

            return statement.executeUpdate() == 1;
        }
    }

    public boolean createStaffAccount(String username, String password)
            throws SQLException {

        if (usernameExists(username)) {
            return false;
        }

        User staff = new User(
                0, username, password, "Staff", "Approved"
        );

        return registerUser(staff);
    }

    // Login only when the account has the correct approved status.
    public User loginUser(String username, String password) throws SQLException {

        String sql = """
                SELECT user_id, username, password, role, status
                FROM Users
                WHERE username = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    String storedPassword = resultSet.getString("password");
                    String role = resultSet.getString("role");
                    String status = resultSet.getString("status");

                    boolean allowedStatus = false;

                    if (role != null && status != null) {

                        if (role.equalsIgnoreCase("Admin")) {
                            allowedStatus = status.equalsIgnoreCase("Active");
                        } else if (role.equalsIgnoreCase("Staff")
                                || role.equalsIgnoreCase("Pet Owner")) {
                            allowedStatus = status.equalsIgnoreCase("Approved");
                        }
                    }

                    if (allowedStatus &&
                            PasswordUtil.checkPassword(password, storedPassword)) {

                        return new User(
                                resultSet.getInt("user_id"),
                                resultSet.getString("username"),
                                storedPassword,
                                role,
                                normalizeStatus(role, status)
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
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String role = resultSet.getString("role");

                users.add(new User(
                        resultSet.getInt("user_id"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        role,
                        normalizeStatus(role, resultSet.getString("status"))
                ));
            }
        }

        return users;
    }

    public List<User> getPendingUsers() throws SQLException {
        List<User> users = new ArrayList<>();

        String sql = """
                SELECT user_id, username, password, role, status
                FROM Users
                WHERE LOWER(status) = 'pending'
                ORDER BY user_id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String role = resultSet.getString("role");

                users.add(new User(
                        resultSet.getInt("user_id"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        role,
                        normalizeStatus(role, resultSet.getString("status"))
                ));
            }
        }

        return users;
    }

    public boolean approveUser(int userId) throws SQLException {

        String sql = """
                UPDATE Users
                SET status = 'Approved'
                WHERE user_id = ?
                AND LOWER(status) = 'pending'
                AND LOWER(role) IN ('staff', 'pet owner')
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            return statement.executeUpdate() == 1;
        }
    }

    // Only Staff and Pet Owner accounts can be updated.
    public boolean updateUserRoleAndStatus(
            int userId,
            String newRole,
            String newStatus
    ) throws SQLException {

        String role = normalizeRole(newRole);

        if (role == null || role.equals("Admin")) {
            return false;
        }

        String status = normalizeStatus(role, newStatus);

        if (status == null) {
            return false;
        }

        String sql = """
                UPDATE Users
                SET role = ?, status = ?
                WHERE user_id = ?
                AND LOWER(role) != 'admin'
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, role);
            statement.setString(2, status);
            statement.setInt(3, userId);

            return statement.executeUpdate() == 1;
        }
    }

    // Check whether an Admin account already exists.
    private boolean hasAdminAccount() throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM Users
                WHERE LOWER(role) = 'admin'
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            return resultSet.next() && resultSet.getInt(1) > 0;
        }
    }

    // Normalize role names.
    private String normalizeRole(String role) {

        if (role == null) {
            return null;
        }

        if (role.equalsIgnoreCase("Admin")) {
            return "Admin";
        }

        if (role.equalsIgnoreCase("Staff")) {
            return "Staff";
        }

        if (role.equalsIgnoreCase("Pet Owner")) {
            return "Pet Owner";
        }

        return null;
    }

    // Admin = Active; Staff and Pet Owner = Approved or Pending.
    private String normalizeStatus(String role, String status) {

        if (role == null || status == null) {
            return null;
        }

        if (role.equalsIgnoreCase("Admin")) {
            return status.equalsIgnoreCase("Active") ? "Active" : null;
        }

        if (role.equalsIgnoreCase("Staff")
                || role.equalsIgnoreCase("Pet Owner")) {

            if (status.equalsIgnoreCase("Approved")) {
                return "Approved";
            }

            if (status.equalsIgnoreCase("Pending")) {
                return "Pending";
            }
        }

        return null;
    }

    // Create the single temporary Admin account.
    public boolean createTemporaryAdmin() throws SQLException {

        String username = "admin";
        String password = "admin123";

        String checkSql = """
                SELECT user_id
                FROM Users
                WHERE LOWER(role) = 'admin'
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(checkSql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {

                int adminId = resultSet.getInt("user_id");

                String updateSql = """
                        UPDATE Users
                        SET status = 'Active'
                        WHERE user_id = ?
                        """;

                try (PreparedStatement updateStatement =
                             connection.prepareStatement(updateSql)) {

                    updateStatement.setInt(1, adminId);
                    updateStatement.executeUpdate();
                }

                return true;
            }
        }

        if (usernameExists(username)) {

            String updateSql = """
                    UPDATE Users
                    SET role = 'Admin', status = 'Active'
                    WHERE username = ?
                    """;

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(updateSql)) {

                statement.setString(1, username);

                return statement.executeUpdate() == 1;
            }
        }

        User admin = new User(
                0, username, password, "Admin", "Active"
        );

        return registerUser(admin);
    }
}
