
package ui;

import dao.UserDao;
import dao.ActivityLogDao;
import model.User;

import java.sql.SQLException;
import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final ActivityLogDao activityLogDao;
    private final Scanner scanner;

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.activityLogDao = new ActivityLogDao();
        this.scanner = scanner;
    }

    public void start() {
        while (true) {
            System.out.println("======================================");
            System.out.println("|          PET CLINIC SYSTEM         |");
            System.out.println("======================================");
            System.out.println("| 1. Register                        |");
            System.out.println("| 2. Login                           |");
            System.out.println("| 3. Exit                            |");
            System.out.println("======================================");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> register();
                case "2" -> login();
                case "3" -> {
                    System.out.println(
                            "Thank you for using Pet Clinic System!"
                    );
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void register() {
        try {
            System.out.println("======================================");
            System.out.println("|        PET OWNER REGISTRATION      |");
            System.out.println("======================================");

            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            if (username.isEmpty() || password.isEmpty()) {
                System.out.println(
                        "Username and password are required."
                );
                return;
            }

            if (userDao.usernameExists(username)) {
                System.out.println("Username already exists.");
                return;
            }

            User user = new User(
                    0,
                    username,
                    password,
                    "Pet Owner",
                    "Pending"
            );

            if (userDao.registerUser(user)) {
                System.out.println("Registration successful!");
                System.out.println(
                        "Your account is pending. Please wait for Admin approval."
                );
            } else {
                System.out.println("Registration failed.");
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private void login() {
        try {
            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            User user = userDao.loginUser(username, password);

            if (user == null) {
                System.out.println(
                        "Invalid username, password, or inactive account."
                );
                return;
            }

            System.out.println(
                    "Login successful! Welcome, " + user.username() + "!"
            );

            // Record successful login
            try {
                activityLogDao.logAction(
                        user.userId(),
                        "USER_LOGIN",
                        "User " + user.username()
                                + " (ID: " + user.userId()
                                + ") logged in."
                );

                System.out.println("Login activity recorded.");

            } catch (SQLException e) {
                System.out.println(
                        "Warning: Login activity could not be recorded: "
                                + e.getMessage()
                );
            }

            RoleMenu roleMenu = new RoleMenu(scanner, userDao);
            roleMenu.showMenu(user);

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
