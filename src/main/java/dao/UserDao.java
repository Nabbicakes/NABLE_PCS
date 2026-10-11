
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
                0,
                username,
                password,
                "Staff",
                "Active"
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

    public List<User> getPendingUsers() throws SQLException {
        List<User> users = new ArrayList<>();

        String sql = """
                SELECT user_id, username, password, role, status
                FROM Users
                WHERE status = 'Pending'
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

    public boolean activateUser(int userId) throws SQLException {
        String sql = """
                UPDATE Users
                SET status = 'Active'
                WHERE user_id = ? AND status = 'Pending'
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            return statement.executeUpdate() == 1;
        }
    }

    public boolean createTemporaryAdmin() throws SQLException {
        String username = "admin";

        if (usernameExists(username)) {
            return false;
        }

        User admin = new User(
                0,
                username,
                "12345",
                "Admin",
                "Active"
        );

        return registerUser(admin);
    }
}
