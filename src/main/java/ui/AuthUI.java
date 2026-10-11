
package ui;

import dao.UserDao;
import model.User;

import java.sql.SQLException;
import java.util.Scanner;

public class AuthUI {

    private final Scanner scanner = new Scanner(System.in);
    private final UserDao userDao = new UserDao();

    public void showMenu() {
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
                case "1":
                    register();
                    break;

                case "2":
                    login();
                    break;

                case "3":
                    System.out.println("Thank you for using Pet Clinic System!");
                    return;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void register() {
        try {
            System.out.println("\n=== PET OWNER REGISTRATION ===");

            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            if (username.isEmpty() || password.isEmpty()) {
                System.out.println("Username and password are required.");
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
                System.out.println("Your account is pending approval.");
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
            String username = scanner.nextLine();

            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            User user = userDao.loginUser(username, password);

            if (user == null) {
                System.out.println(
                        "Invalid username, password, or account pending approval."
                );
                return;
            }

            System.out.println(
                    "Login successful! Welcome, " + user.username() + "!"
            );

            RoleMenu roleMenu = new RoleMenu(scanner, userDao);
            roleMenu.showMenu(user);

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

}


