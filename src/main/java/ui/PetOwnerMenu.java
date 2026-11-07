
package ui;

import dao.UserDao;
import dao.ActivityLogDao;
import model.User;

import java.sql.SQLException;
import java.util.Scanner;

public class PetOwnerMenu {

    private final Scanner scanner;
    private final UserDao userDao;
    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    public PetOwnerMenu(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public void showMenu(User user) {
        while (true) {
            System.out.println();
            System.out.println("======================================");
            System.out.println("|          PET CLINIC SYSTEM         |");
            System.out.println("|             PET OWNER              |");
            System.out.println("======================================");
            System.out.println("| 1. View My Pets                    |");
            System.out.println("| 2. View Appointments               |");
            System.out.println("| 3. Logout                          |");
            System.out.println("| 4. Exit                            |");
            System.out.println("======================================");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.println("View My Pets is not implemented yet.");
                    break;

                case "2":
                    System.out.println("View Appointments is not implemented yet.");
                    break;

                case "3":
                    try {
                        activityLogDao.logAction(
                                user.userId(),
                                "USER_LOGOUT",
                                "Pet Owner " + user.username()
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

                case "4":
                    System.out.println("Thank you for using Pet Clinic System!");
                    return;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
