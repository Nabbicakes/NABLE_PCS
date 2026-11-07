
package ui;

import dao.UserDao;
import dao.ActivityLogDao;
import model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final Scanner scanner;
    private final UserDao userDao;
    private final ActivityLogDao activityLogDao = new ActivityLogDao();

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
            System.out.println("| 4. Update User Role and Status     |");
            System.out.println("| 5. View Activity Logs              |");
            System.out.println("| 6. Logout                          |");
            System.out.println("| 7. Exit                            |");
            System.out.println("======================================");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    createStaffAccount();
                    break;

                case "2":
                    manageUserAccounts(user);
                    break;

                case "3":
                    viewAllUsers();
                    break;

                case "4":
                    updateUser(user);
                    break;

                case "5":
                    System.out.println(
                            "View Activity Logs is not implemented yet."
                    );
                    break;

                case "6":
                    try {
                        activityLogDao.logAction(
                                user.userId(),
                                "USER_LOGOUT",
                                "Admin " + user.username()
                                        + " (ID: " + user.userId()
                                        + ") logged out."
                        );

                        System.out.println("Logged out successfully!");

                    } catch (SQLException e) {
                        System.out.println(
                                "Warning: Logout activity could not be recorded: "
                                        + e.getMessage()
                        );
                        System.out.println("Logged out successfully!");
                    }

                    return;

                case "7":
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
                System.out.println("Status: Approved");
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

    // View pending registrations and approve an account
    private void manageUserAccounts(User admin) {
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

            User selectedUser = pendingUsers.stream()
                    .filter(account -> account.userId() == userId)
                    .findFirst()
                    .orElse(null);

            if (selectedUser == null) {
                System.out.println(
                        "That ID is not in the pending accounts list."
                );
                return;
            }

            if (userDao.approveUser(userId)) {
                System.out.println("Account approved successfully!");
                System.out.println("The user can now log in.");

                activityLogDao.logAction(
                        admin.userId(),
                        "USER_APPROVED",
                        "Admin " + admin.username()
                                + " (ID: " + admin.userId()
                                + ") approved the account of "
                                + selectedUser.username()
                                + " (ID: " + selectedUser.userId()
                                + ")."
                );

            } else {
                System.out.println("Account could not be approved.");
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Update another user's role and status
    private void updateUser(User admin) {
        try {
            List<User> users = userDao.getAllUsers();

            System.out.println();
            System.out.println("======================================");
            System.out.println("|       UPDATE USER ROLE/STATUS      |");
            System.out.println("======================================");

            if (users.isEmpty()) {
                System.out.println("No registered users found.");
                return;
            }

            displayUserTable(users);

            System.out.print("\nEnter the User ID to update (0 to cancel): ");
            int userId = readIntInput();

            if (userId == 0) {
                System.out.println("Update cancelled.");
                return;
            }

            if (userId == -1) {
                return;
            }

            if (userId == admin.userId()) {
                System.out.println(
                        "WARNING: Modifying the currently active admin account is restricted."
                );
                return;
            }

            User selectedUser = users.stream()
                    .filter(account -> account.userId() == userId)
                    .findFirst()
                    .orElse(null);

            if (selectedUser == null) {
                System.out.println("User ID not found.");
                return;
            }

            if (selectedUser.role().equalsIgnoreCase("Admin")) {
                System.out.println("Admin accounts cannot be modified.");
                return;
            }

            System.out.println("\nSelected User: " + selectedUser.username());
            System.out.println("Current Role: " + selectedUser.role());
            System.out.println("Current Status: " + selectedUser.status());

            System.out.println("\nAvailable Roles:");
            System.out.println("1. Staff");
            System.out.println("2. Pet Owner");
            System.out.println("0. Cancel");

            System.out.print("Choose new role: ");
            String roleChoice = scanner.nextLine().trim();

            if (roleChoice.equals("0")) {
                System.out.println("Update cancelled.");
                return;
            }

            String newRole;

            switch (roleChoice) {
                case "1":
                    newRole = "Staff";
                    break;
                case "2":
                    newRole = "Pet Owner";
                    break;
                default:
                    System.out.println("Invalid role choice.");
                    return;
            }

            System.out.println("\nAvailable Statuses:");
            System.out.println("1. Approved");
            System.out.println("2. Pending");
            System.out.println("0. Cancel");

            System.out.print("Choose new status: ");
            String statusChoice = scanner.nextLine().trim();

            if (statusChoice.equals("0")) {
                System.out.println("Update cancelled.");
                return;
            }

            String newStatus;

            switch (statusChoice) {
                case "1":
                    newStatus = "Approved";
                    break;
                case "2":
                    newStatus = "Pending";
                    break;
                default:
                    System.out.println("Invalid status choice.");
                    return;
            }

            boolean updated = userDao.updateUserRoleAndStatus(
                    userId,
                    newRole,
                    newStatus
            );

            if (updated) {
                System.out.println("\nUser updated successfully!");
                System.out.println("New Role: " + newRole);
                System.out.println("New Status: " + newStatus);

                try {
                    activityLogDao.logAction(
                            admin.userId(),
                            "USER_UPDATED",
                            "Admin " + admin.username()
                                    + " (ID: " + admin.userId()
                                    + ") updated user "
                                    + selectedUser.username()
                                    + " (ID: " + selectedUser.userId()
                                    + ") from Role: " + selectedUser.role()
                                    + ", Status: " + selectedUser.status()
                                    + " to Role: " + newRole
                                    + ", Status: " + newStatus + "."
                    );

                    System.out.println("Update activity recorded.");

                } catch (SQLException e) {
                    System.out.println(
                            "Warning: Update activity could not be recorded: "
                                    + e.getMessage()
                    );
                }

            } else {
                System.out.println("User could not be updated.");
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private int readIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid numeric ID.");
            return -1;
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
