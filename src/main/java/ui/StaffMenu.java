
package ui;

import model.User;
import java.util.Scanner;

public class StaffMenu {

    private final Scanner scanner;

    public StaffMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void showMenu(User user) {
        while (true) {
            System.out.println();
            System.out.println("======================================");
            System.out.println("|          PET CLINIC SYSTEM         |");
            System.out.println("|               STAFF                |");
            System.out.println("======================================");
            System.out.println("| 1. Manage Pet Records              |");
            System.out.println("| 2. Manage Appointments             |");
            System.out.println("| 3. View Payments                   |");
            System.out.println("| 4. Logout                          |");
            System.out.println("| 5. Exit                            |");
            System.out.println("======================================");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.println("Manage Pet Records is not implemented yet.");
                    break;
                case "2":
                    System.out.println("Manage Appointments is not implemented yet.");
                    break;
                case "3":
                    System.out.println("View Payments is not implemented yet.");
                    break;
                case "4":
                    System.out.println("Logged out successfully!");
                    return;
                case "5":
                    System.out.println("Thank you for using Pet Clinic System!");
                    System.exit(0);
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
