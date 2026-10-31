
package ui;

import dao.UserDao;
import model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final Scanner scanner;
    private final UserDao userDao;

    public AdminMenu(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public void showMenu(User user) {
        while (true) {
            System.out.println();
            System.out.println("======================================");
            System.out.println("|          PET CLINIC SYSTEM         |");
            System.out.println("|               ADMIN                |");
            System.out.println("======================================");
            System.out.println("| 1. Create Staff Account            |");
            System.out.println("| 2. View Pending Registrations      |");
            System.out.println("| 3. View All System Users           |");
            System.out.println("| 4. View Activity Logs              |");
            System.out.println("| 5. Logout                          |");
            System.out.println("| 6. Exit                            |");
            System.out.println("======================================");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    createStaffAccount();
                    break;

                case "2":
                    manageUserAccounts();
                    break;

                case "3":
                    viewAllUsers();
                    break;

                case "4":
                    System.out.println(
                            "View Activity Logs is not implemented yet."
                    );
                    break;

                case "5":
                    System.out.println("Logged out successfully!");
                    return;

                case "6":
                    System.out.println(
                            "Thank you for using Pet Clinic System!"
                    );
                    return;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void createStaffAccount() {
        try {
            System.out.println("======================================");
            System.out.println("|        CREATE STAFF ACCOUNT        |");
            System.out.println("======================================");

            System.out.print("Enter staff username: ");
            String username = scanner.nextLine().trim();

            if (username.isEmpty()) {
                System.out.println("Username cannot be empty.");
                return;
            }

            System.out.print("Enter staff password: ");
            String password = scanner.nextLine();

            if (password.isBlank()) {
                System.out.println("Password cannot be empty.");
                return;
            }

            boolean created = userDao.createStaffAccount(
                    username,
                    password
            );

            if (created) {
                System.out.println("======================================");
                System.out.println("Staff account created successfully!");
                System.out.println("Username: " + username);
                System.out.println("Role: Staff");
                System.out.println("Status: APPROVED");
                System.out.println(
                        "Give the login credentials to the employee privately."
                );
                System.out.println("======================================");
            } else {
                System.out.println(
                        "That username already exists. Please try another."
                );
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // View all pending registrations and approve an account
    private void manageUserAccounts() {
        try {
            List<User> pendingUsers = userDao.getPendingUsers();

            System.out.println();
            System.out.println("======================================");
            System.out.println("|        PENDING REGISTRATIONS       |");
            System.out.println("======================================");

            if (pendingUsers.isEmpty()) {
                System.out.println("There are no pending accounts.");
                return;
            }

            displayUserTable(pendingUsers);

            System.out.print(
                    "\nEnter the User ID to approve (0 to cancel): "
            );

            String input = scanner.nextLine().trim();

            int userId;

            try {
                userId = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid numeric ID.");
                return;
            }

            if (userId == 0) {
                System.out.println("Approval cancelled.");
                return;
            }

            boolean isPending = pendingUsers.stream()
                    .anyMatch(account -> account.userId() == userId);

            if (!isPending) {
                System.out.println(
                        "That ID is not in the pending accounts list."
                );
                return;
            }

            if (userDao.approveUser(userId)) {
                System.out.println("Account approved successfully!");
                System.out.println("The user can now log in.");
            } else {
                System.out.println("Account could not be approved.");
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // View all registered users regardless of role or status
    private void viewAllUsers() {
        try {
            List<User> users = userDao.getAllUsers();

            System.out.println();
            System.out.println("======================================");
            System.out.println("|          ALL SYSTEM USERS          |");
            System.out.println("======================================");

            if (users.isEmpty()) {
                System.out.println("No registered users found.");
                return;
            }

            displayUserTable(users);

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Display users in a formatted table
    private void displayUserTable(List<User> users) {
        String border = "+-------+-----------------+--------------+------------+";

        System.out.println(border);

        System.out.printf(
                "| %-5s | %-15s | %-12s | %-10s |%n",
                "ID",
                "USERNAME",
                "ROLE",
                "STATUS"
        );

        System.out.println(border);

        for (User user : users) {
            System.out.printf(
                    "| %-5d | %-15s | %-12s | %-10s |%n",
                    user.userId(),
                    user.username(),
                    user.role(),
                    user.status()
            );
        }

        System.out.println(border);
    }
}
