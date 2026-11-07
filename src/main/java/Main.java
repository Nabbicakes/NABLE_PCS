
import dao.DatabaseInitializer;
import dao.UserDao;
import ui.Authentication;

import java.sql.SQLException;
import java.util.Scanner;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        try {
            System.out.println("Database location: "
                    + Path.of("PetClinic.db").toAbsolutePath().normalize());

            DatabaseInitializer.initialize();

            UserDao userDao = new UserDao();
            Scanner scanner = new Scanner(System.in);

            boolean created = userDao.createTemporaryAdmin();

            if (created) {
                System.out.println("Temporary Admin account created!");
            }

            Authentication authentication =
                    new Authentication(userDao, scanner);

            authentication.start();

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
