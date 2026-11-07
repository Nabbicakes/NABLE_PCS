
package ui;

import dao.UserDao;
import dao.ActivityLogDao;
import model.User;
import model.ActivityLog;

import java.sql.SQLException;
import java.util.ArrayList;
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
                    viewActivityLogs();
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

    // View activity logs
    private void viewActivityLogs() {
        while (true) {
            System.out.println();
            System.out.println("======================================");
            System.out.println("|          ACTIVITY LOGS             |");
            System.out.println("======================================");
            System.out.println("| 1. View All Activity Logs           |");
            System.out.println("| 2. Filter Logs by User ID           |");
            System.out.println("| 3. Back                             |");
            System.out.println("======================================");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    displayActivityLogs(false, 0);
                    break;

                case "2":
                    System.out.print("\nEnter User ID (0 to cancel): ");
                    int userId = readIntInput();

                    if (userId == 0) {
                        System.out.println("Filter cancelled.");
                    } else if (userId != -1) {
                        displayActivityLogs(true, userId);
                    }
                    break;

                case "3":
                    return;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // Retrieve and display activity logs
    private void displayActivityLogs(boolean filterByUser, int userId) {
        try {
            List<ActivityLog> logs;

            if (filterByUser) {
                logs = activityLogDao.getLogsByUserId(userId);
            } else {
                logs = activityLogDao.getAllLogs();
            }

            System.out.println();
            System.out.println("======================================");
            System.out.println(filterByUser
                    ? "|       FILTERED ACTIVITY LOGS       |"
                    : "|          ALL ACTIVITY LOGS         |");
            System.out.println("======================================");

            if (logs.isEmpty()) {
                System.out.println(filterByUser
                        ? "No activity logs found for User ID: " + userId
                        : "No activity logs found.");
                return;
            }

            displayActivityLogTable(logs);

            System.out.println("Total Records: " + logs.size());

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Display activity logs with automatic text wrapping
    private void displayActivityLogTable(List<ActivityLog> logs) {

        int[] widths = {4, 7, 26, 45, 19};

        System.out.println();

        printActivityLogBorder(widths);

        System.out.println(
                "| " + padRight("ID", widths[0])
                        + " | " + padRight("USER ID", widths[1])
                        + " | " + padRight("ACTION TYPE", widths[2])
                        + " | " + padRight("DESCRIPTION", widths[3])
                        + " | " + padRight("TIMESTAMP", widths[4])
                        + " |"
        );

        printActivityLogBorder(widths);

        for (ActivityLog log : logs) {

            List<String> id = wrapText(
                    String.valueOf(log.id()), widths[0]);

            List<String> userId = wrapText(
                    String.valueOf(log.userId()), widths[1]);

            List<String> actionType = wrapText(
                    log.actionType(), widths[2]);

            List<String> description = wrapText(
                    log.description(), widths[3]);

            List<String> timestamp = wrapText(
                    log.timestamp(), widths[4]);

            int lines = Math.max(
                    Math.max(
                            Math.max(id.size(), userId.size()),
                            Math.max(actionType.size(), description.size())
                    ),
                    timestamp.size()
            );

            for (int i = 0; i < lines; i++) {
                System.out.println(
                        "| " + getWrappedLine(id, i, widths[0])
                                + " | " + getWrappedLine(userId, i, widths[1])
                                + " | " + getWrappedLine(actionType, i, widths[2])
                                + " | " + getWrappedLine(description, i, widths[3])
                                + " | " + getWrappedLine(timestamp, i, widths[4])
                                + " |"
                );
            }

            printActivityLogBorder(widths);
        }
    }

    // Print table borders according to column widths
    private void printActivityLogBorder(int[] widths) {
        StringBuilder border = new StringBuilder("+");

        for (int width : widths) {
            border.append("-".repeat(width + 2)).append("+");
        }

        System.out.println(border);
    }

    // Wrap long text into multiple lines
    private List<String> wrapText(String text, int width) {
        List<String> lines = new ArrayList<>();

        if (text == null || text.isBlank()) {
            lines.add("");
            return lines;
        }

        String[] words = text.trim().split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {

            // Split a single word if it is longer than the column width
            while (word.length() > width) {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine.setLength(0);
                }

                lines.add(word.substring(0, width));
                word = word.substring(width);
            }

            if (currentLine.length() == 0) {
                currentLine.append(word);

            } else if (currentLine.length() + 1 + word.length() <= width) {
                currentLine.append(" ").append(word);

            } else {
                lines.add(currentLine.toString());
                currentLine.setLength(0);
                currentLine.append(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    // Get a line from wrapped text and align it inside the column
    private String getWrappedLine(List<String> lines, int index, int width) {
        String value = index < lines.size() ? lines.get(index) : "";
        return padRight(value, width);
    }

    // Align text to the right side of its available space
    private String padRight(String text, int width) {
        if (text == null) {
            text = "";
        }

        if (text.length() > width) {
            text = text.substring(0, width);
        }

        return String.format("%-" + width + "s", text);
    }

    // Read a valid numeric input
    private int readIntInput() {
        while (true) {
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid numeric ID: ");
            }
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
