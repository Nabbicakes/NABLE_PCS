
package ui;

import dao.UserDao;
import model.User;

import java.util.Scanner;

public class RoleMenu {

    private final Scanner scanner;
    private final UserDao userDao;

    public RoleMenu(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public void showMenu(User user) {
        String role = user.role().trim().toLowerCase();

        switch (role) {
            case "admin":
                AdminMenu adminMenu = new AdminMenu(scanner, userDao);
                adminMenu.showMenu(user);
                break;

            case "staff":
                StaffMenu staffMenu = new StaffMenu(scanner);
                staffMenu.showMenu(user);
                break;

            case "pet owner":
                PetOwnerMenu petOwnerMenu = new PetOwnerMenu();
                petOwnerMenu.showMenu(user);
                break;

            default:
                System.out.println("Unknown account role. Logging out.");
                break;
        }
    }
}
