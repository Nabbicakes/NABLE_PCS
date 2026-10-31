
package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:PetClinic.db";

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");
            return connection;
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
    }

    public static void initializeDatabase() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS Users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL DEFAULT 'Pet Owner',
                    status TEXT NOT NULL DEFAULT 'Pending'
                )
                """;

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(sql);
        }
    }
}
