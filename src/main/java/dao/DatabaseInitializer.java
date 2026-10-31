
package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() throws SQLException {

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("""
    CREATE TABLE IF NOT EXISTS Pet_Owners (
        owner_id INTEGER PRIMARY KEY AUTOINCREMENT,
        owner_name TEXT NOT NULL,
        contact TEXT,
        address TEXT
    )
    """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS Pet_Owners (
                    owner_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL UNIQUE,
                    owner_name TEXT NOT NULL,
                    contact TEXT,
                    address TEXT,
                    FOREIGN KEY (user_id) REFERENCES Users(user_id)
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS Pets (
                    pet_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    owner_id INTEGER NOT NULL,
                    pet_name TEXT NOT NULL,
                    species TEXT NOT NULL,
                    breed TEXT,
                    age INTEGER,
                    gender TEXT,
                    status TEXT NOT NULL,
                    FOREIGN KEY (owner_id) REFERENCES Pet_Owners(owner_id)
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS Veterinarians (
                    veterinarian_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    specialization TEXT,
                    contact TEXT,
                    availability TEXT,
                    status TEXT NOT NULL
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS Appointments (
                    appointment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    pet_id INTEGER NOT NULL,
                    veterinarian_id INTEGER NOT NULL,
                    appointment_date TEXT NOT NULL,
                    appointment_time TEXT NOT NULL,
                    reason TEXT,
                    status TEXT NOT NULL DEFAULT 'Scheduled',
                    FOREIGN KEY (pet_id) REFERENCES Pets(pet_id),
                    FOREIGN KEY (veterinarian_id)
                        REFERENCES Veterinarians(veterinarian_id)
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS Medical_Records (
                    record_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    pet_id INTEGER NOT NULL,
                    appointment_id INTEGER NOT NULL,
                    diagnosis TEXT,
                    treatment TEXT,
                    notes TEXT,
                    FOREIGN KEY (pet_id) REFERENCES Pets(pet_id),
                    FOREIGN KEY (appointment_id)
                        REFERENCES Appointments(appointment_id)
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS Payments (
                    payment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    appointment_id INTEGER NOT NULL UNIQUE,
                    amount REAL NOT NULL,
                    payment_date TEXT,
                    payment_status TEXT NOT NULL DEFAULT 'Pending',
                    FOREIGN KEY (appointment_id)
                        REFERENCES Appointments(appointment_id)
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS Activity_Logs (
                    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    action TEXT NOT NULL,
                    details TEXT,
                    log_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES Users(user_id)
                )
                """);
        }
    }
}
