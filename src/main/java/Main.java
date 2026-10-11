
import dao.DatabaseConnection;
import dao.UserDao;
import ui.AuthUI;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        try {
            // Create the database and Users table if they do not exist.
            DatabaseConnection.initializeDatabase();

            // Create the temporary Admin account if it does not exist.
            UserDao userDao = new UserDao();
            boolean created = userDao.createTemporaryAdmin();

            if (created) {
                System.out.println("Temporary Admin account created!");

            }

            // Open the main menu.
            AuthUI authUI = new AuthUI();
            authUI.showMenu();

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
