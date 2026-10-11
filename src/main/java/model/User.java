
package model;

public record User(
        int userId,
        String username,
        String password,
        String role,
        String status
) {
}
