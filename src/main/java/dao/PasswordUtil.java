
package dao;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // Hash a password before saving it to the database
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    // Check if the entered password matches the stored hash
    public static boolean checkPassword(
            String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
