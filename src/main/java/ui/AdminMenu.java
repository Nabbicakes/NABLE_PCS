
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
            System.out.println("| 2. View Inactive Accounts          |");
            System.out.println("| 3. View All System Users           |");
            System.out.println("| 4. Update User Account              |");
            System.out.println("| 5. View Activity Logs              |");
            System.out.println("| 6. Logout                          |");
            System.out.println("| 7. Exit                            |");
            System.out.println("======================================");

            int choice = readIntInput("Choose an option: ");

            switch (choice) {
                case 1 -> createStaffAccount();
                case 2 -> viewInactiveAccounts();
                case 3 -> viewAllUsers();
                case 4 -> updateUserAccount(user);
                case 5 -> System.out.println(
                        "View Activity Logs is not implemented yet."
                );
                case 6 -> {
                    System.out.println("Logged out successfully!");
                    return;
                }
                case 7 -> {
                    System.out.println(
                            "Thank you for using Pet Clinic System!"
                    );
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
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
                    username, password
            );

            if (created) {
                System.out.println("Staff account created successfully!");
                System.out.println("Username: " + username);
                System.out.println("Role: Staff");
                System.out.println("Status: Active");
            } else {
                System.out.println(
                        "That username already exists. Please try another."
                );
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private void viewInactiveAccounts() {
        try {
            List<User> inactiveUsers = userDao.getInactiveUsers();

            System.out.println();
            System.out.println("======================================");
            System.out.println("|          INACTIVE ACCOUNTS         |");
            System.out.println("======================================");

            if (inactiveUsers.isEmpty()) {
                System.out.println("There are no inactive accounts.");
                return;
            }

            displayUserTable(inactiveUsers);

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

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

    private void updateUserAccount(User currentAdmin) {
        try {
            List<User> users = userDao.getAllUsers();

            System.out.println();
            System.out.println("======================================");
            System.out.println("|         UPDATE USER ACCOUNT        |");
            System.out.println("======================================");

            if (users.isEmpty()) {
                System.out.println("No registered users found.");
                return;
            }

            displayUserTable(users);

            int targetUserId = readIntInput(
                    "Enter the User ID to update (0 to cancel): "
            );

            if (targetUserId == 0) {
                System.out.println("Update cancelled.");
                return;
            }

            User targetUser = users.stream()
                    .filter(account -> account.userId() == targetUserId)
                    .findFirst()
                    .orElse(null);

            if (targetUser == null) {
                System.out.println("User ID not found.");
                return;
            }

            if (targetUserId == currentAdmin.userId()) {
                System.out.println(
                        "WARNING: Modifying the currently active admin account is restricted."
                );
                return;
            }

            System.out.println("Selected username: " + targetUser.username());
            System.out.println("Current role: " + targetUser.role());
            System.out.println("Current status: " + targetUser.status());

            String newRole = readValidRole();

            if (newRole == null) {
                System.out.println("Update cancelled.");
                return;
            }

            String newStatus = readValidStatus();

            if (newStatus == null) {
                System.out.println("Update cancelled.");
                return;
            }

            System.out.println();
            System.out.println("New role: " + newRole);
            System.out.println("New status: " + newStatus);

            if (userDao.updateUserRoleAndStatus(
                    targetUserId, newRole, newStatus)) {

                System.out.println("User account updated successfully!");

            } else {
                System.out.println("User account could not be updated.");
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(
                        "Invalid input. Please enter a valid number."
                );
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.println();
            System.out.println("======================================");
            System.out.println("| Choose a new role:                 |");
            System.out.println("| 1. Admin                           |");
            System.out.println("| 2. Staff                           |");
            System.out.println("| 3. Pet Owner                       |");
            System.out.println("| 0. Cancel                          |");
            System.out.println("======================================");

            int choice = readIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    return "Admin";
                case 2:
                    return "Staff";
                case 3:
                    return "Pet Owner";
                case 0:
                    return null;
                default:
                    System.out.println("Invalid role. Please try again.");
            }
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.println();
            System.out.println("======================================");
            System.out.println("| Choose a new status:               |");
            System.out.println("| 1. Active                          |");
            System.out.println("| 2. Inactive                        |");
            System.out.println("| 0. Cancel                          |");
            System.out.println("======================================");

            int choice = readIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    return "Active";
                case 2:
                    return "Inactive";
                case 0:
                    return null;
                default:
                    System.out.println("Invalid status. Please try again.");
            }
        }
    }

    private void displayUserTable(List<User> users) {
        String border = "+-------+-----------------+--------------+------------+";

        System.out.println(border);

        System.out.printf(
                "| %-5s | %-15s | %-12s | %-10s |%n",
                "ID", "USERNAME", "ROLE", "STATUS"
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
