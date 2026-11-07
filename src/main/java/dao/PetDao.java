
package dao;

import model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PetDao {

    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    // Get approved Pet Owner accounts
    public List<String[]> getApprovedPetOwners() throws SQLException {
        List<String[]> owners = new ArrayList<>();

        String sql = """
            SELECT user_id, username
            FROM Users
            WHERE LOWER(role) = 'pet owner'
              AND LOWER(status) = 'approved'
            ORDER BY username
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                owners.add(new String[]{
                        String.valueOf(resultSet.getInt("user_id")),
                        resultSet.getString("username")
                });
            }
        }

        return owners;
    }

    // Register a pet and connect it to its owner
    public boolean registerPet(
            int ownerUserId,
            String ownerName,
            String contact,
            String address,
            String petName,
            String species,
            String breed,
            int age,
            String gender,
            int staffUserId
    ) throws SQLException {

        String ownerSql = """
            SELECT owner_id
            FROM Pet_Owners
            WHERE user_id = ?
            """;

        String insertOwnerSql = """
            INSERT INTO Pet_Owners
            (owner_name, contact, adress, user_id)
            VALUES (?, ?, ?, ?)
            """;

        String insertPetSql = """
            INSERT INTO Pets
            (owner_id, pet_name, species, breed, age, gender, status)
            VALUES (?, ?, ?, ?, ?, ?, 'Active')
            """;

        int ownerId;

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                // Check whether an owner record already exists
                try (PreparedStatement statement =
                             connection.prepareStatement(ownerSql)) {

                    statement.setInt(1, ownerUserId);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            ownerId = resultSet.getInt("owner_id");
                        } else {
                            // Create owner record
                            try (PreparedStatement insertOwner =
                                         connection.prepareStatement(
                                                 insertOwnerSql,
                                                 Statement.RETURN_GENERATED_KEYS)) {

                                insertOwner.setString(1, ownerName);
                                insertOwner.setString(2, contact);
                                insertOwner.setString(3, address);
                                insertOwner.setInt(4, ownerUserId);

                                insertOwner.executeUpdate();

                                try (ResultSet keys =
                                             insertOwner.getGeneratedKeys()) {

                                    if (!keys.next()) {
                                        throw new SQLException(
                                                "Could not retrieve owner ID."
                                        );
                                    }

                                    ownerId = keys.getInt(1);
                                }
                            }
                        }
                    }
                }

                // Register pet
                int petId;

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     insertPetSql,
                                     Statement.RETURN_GENERATED_KEYS)) {

                    statement.setInt(1, ownerId);
                    statement.setString(2, petName);
                    statement.setString(3, species);
                    statement.setString(4, breed);
                    statement.setInt(5, age);
                    statement.setString(6, gender);

                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                    "Could not retrieve pet ID."
                            );
                        }

                        petId = keys.getInt(1);
                    }
                }

                connection.commit();

                // Record activity
                try {
                    activityLogDao.logAction(
                            staffUserId,
                            "PET_REGISTERED",
                            "Staff registered Pet #" + petId
                                    + " (" + petName + ") for Owner #"
                                    + ownerId + "."
                    );
                } catch (SQLException e) {
                    System.out.println(
                            "Warning: Pet was registered, but activity "
                                    + "logging failed: " + e.getMessage()
                    );
                }

                System.out.println(
                        "Pet registered successfully! Pet ID: " + petId
                );

                return true;

            } catch (SQLException e) {
                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    // Get all registered pets
    public List<String[]> getAllPets() throws SQLException {
        List<String[]> pets = new ArrayList<>();

        String sql = """
            SELECT p.pet_id, p.pet_name, p.species,
                   p.breed, p.age, p.gender, p.status,
                   o.owner_name
            FROM Pets p
            JOIN Pet_Owners o ON p.owner_id = o.owner_id
            ORDER BY p.pet_id
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                pets.add(new String[]{
                        String.valueOf(resultSet.getInt("pet_id")),
                        resultSet.getString("pet_name"),
                        resultSet.getString("species"),
                        resultSet.getString("breed"),
                        String.valueOf(resultSet.getInt("age")),
                        resultSet.getString("gender"),
                        resultSet.getString("status"),
                        resultSet.getString("owner_name")
                });
            }
        }

        return pets;
    }

    // Get pets belonging to a specific Pet Owner account
    public List<String[]> getPetsByOwnerUserId(int userId)
            throws SQLException {

        List<String[]> pets = new ArrayList<>();

        String sql = """
            SELECT p.pet_id, p.pet_name, p.species,
                   p.breed, p.age, p.gender, p.status
            FROM Pets p
            JOIN Pet_Owners o ON p.owner_id = o.owner_id
            WHERE o.user_id = ?
            ORDER BY p.pet_id
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    pets.add(new String[]{
                            String.valueOf(resultSet.getInt("pet_id")),
                            resultSet.getString("pet_name"),
                            resultSet.getString("species"),
                            resultSet.getString("breed"),
                            String.valueOf(resultSet.getInt("age")),
                            resultSet.getString("gender"),
                            resultSet.getString("status")
                    });
                }
            }
        }

        return pets;
    }
}
