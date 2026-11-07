
package ui;

import dao.UserDao;
import dao.ActivityLogDao;
import dao.PetDao;
import dao.AppointmentDao;
import model.User;
import model.Appointment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class PetOwnerMenu {

    private final Scanner scanner;
    private final UserDao userDao;
    private final ActivityLogDao activityLogDao = new ActivityLogDao();
    private final PetDao petDao = new PetDao();
    private final AppointmentDao appointmentDao = new AppointmentDao();

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
                    viewMyPets(user);
                    break;

                case "2":
                    viewMyAppointments(user);
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
                    System.out.println(
                            "Thank you for using Pet Clinic System!"
                    );
                    return;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // View pets belonging to the logged-in Pet Owner
    private void viewMyPets(User user) {
        try {
            List<String[]> pets =
                    petDao.getPetsByOwnerUserId(user.userId());

            System.out.println();
            System.out.println("======================================");
            System.out.println("|              MY PETS               |");
            System.out.println("======================================");

            if (pets.isEmpty()) {
                System.out.println("You don't have any registered pets yet.");
                return;
            }

            displayMyPetsTable(pets);

            System.out.println("Total Registered Pets: " + pets.size());

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Display the owner's pets in a formatted table
    private void displayMyPetsTable(List<String[]> pets) {

        int[] widths = {7, 20, 15, 18, 5, 12};

        System.out.println();

        printTableBorder(widths);

        System.out.println(
                "| " + padRight("PET ID", widths[0])
                        + " | " + padRight("PET NAME", widths[1])
                        + " | " + padRight("SPECIES", widths[2])
                        + " | " + padRight("BREED", widths[3])
                        + " | " + padRight("AGE", widths[4])
                        + " | " + padRight("STATUS", widths[5])
                        + " |"
        );

        printTableBorder(widths);

        for (String[] pet : pets) {

            List<String> petId = wrapText(pet[0], widths[0]);
            List<String> petName = wrapText(pet[1], widths[1]);
            List<String> species = wrapText(pet[2], widths[2]);
            List<String> breed = wrapText(pet[3], widths[3]);
            List<String> age = wrapText(pet[4], widths[4]);
            List<String> status = wrapText(pet[6], widths[5]);

            int lines = Math.max(
                    Math.max(
                            Math.max(petId.size(), petName.size()),
                            Math.max(species.size(), breed.size())
                    ),
                    Math.max(age.size(), status.size())
            );

            for (int i = 0; i < lines; i++) {
                System.out.println(
                        "| " + getWrappedLine(petId, i, widths[0])
                                + " | " + getWrappedLine(petName, i, widths[1])
                                + " | " + getWrappedLine(species, i, widths[2])
                                + " | " + getWrappedLine(breed, i, widths[3])
                                + " | " + getWrappedLine(age, i, widths[4])
                                + " | " + getWrappedLine(status, i, widths[5])
                                + " |"
                );
            }

            printTableBorder(widths);
        }
    }

    // View appointments belonging to the logged-in Pet Owner
    private void viewMyAppointments(User user) {
        try {
            List<String[]> pets =
                    petDao.getPetsByOwnerUserId(user.userId());

            System.out.println();
            System.out.println("======================================");
            System.out.println("|          MY APPOINTMENTS           |");
            System.out.println("======================================");

            if (pets.isEmpty()) {
                System.out.println(
                        "You don't have any registered pets yet."
                );
                return;
            }

            // Store only the logged-in owner's pet IDs and names
            Set<Integer> ownerPetIds = new HashSet<>();
            Map<Integer, String> petNames = new HashMap<>();

            for (String[] pet : pets) {
                int petId = Integer.parseInt(pet[0]);

                ownerPetIds.add(petId);
                petNames.put(petId, pet[1]);
            }

            // Get veterinarian names
            Map<Integer, String> veterinarianNames = new HashMap<>();

            List<String[]> veterinarians =
                    appointmentDao.getAllVeterinarians();

            for (String[] veterinarian : veterinarians) {
                int veterinarianId =
                        Integer.parseInt(veterinarian[0]);

                veterinarianNames.put(
                        veterinarianId,
                        veterinarian[1]
                );
            }

            // Retrieve appointments and keep only the owner's pets
            List<Appointment> allAppointments =
                    appointmentDao.getAllAppointments();

            List<Appointment> myAppointments = new ArrayList<>();

            for (Appointment appointment : allAppointments) {
                if (ownerPetIds.contains(appointment.petId())) {
                    myAppointments.add(appointment);
                }
            }

            if (myAppointments.isEmpty()) {
                System.out.println(
                        "You don't have any appointments yet."
                );
                return;
            }

            displayMyAppointmentsTable(
                    myAppointments,
                    petNames,
                    veterinarianNames
            );

            System.out.println(
                    "Total Appointments: " + myAppointments.size()
            );

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Display the owner's appointments in a formatted table
    private void displayMyAppointmentsTable(
            List<Appointment> appointments,
            Map<Integer, String> petNames,
            Map<Integer, String> veterinarianNames
    ) {

        int[] widths = {5, 7, 18, 20, 10, 35, 13};

        System.out.println();

        printTableBorder(widths);

        System.out.println(
                "| " + padRight("ID", widths[0])
                        + " | " + padRight("PET ID", widths[1])
                        + " | " + padRight("PET NAME", widths[2])
                        + " | " + padRight("VETERINARIAN", widths[3])
                        + " | " + padRight("DATE", widths[4])
                        + " | " + padRight("REASON", widths[5])
                        + " | " + padRight("STATUS", widths[6])
                        + " |"
        );

        printTableBorder(widths);

        for (Appointment appointment : appointments) {

            String petName = petNames.getOrDefault(
                    appointment.petId(), "Unknown"
            );

            String veterinarianName = veterinarianNames.getOrDefault(
                    appointment.veterinarianId(), "Unknown"
            );

            String dateTime = appointment.appointmentDate()
                    + " " + appointment.appointmentTime();

            List<String> id = wrapText(
                    String.valueOf(appointment.appointmentId()),
                    widths[0]
            );

            List<String> petId = wrapText(
                    String.valueOf(appointment.petId()),
                    widths[1]
            );

            List<String> pet = wrapText(
                    petName,
                    widths[2]
            );

            List<String> veterinarian = wrapText(
                    veterinarianName,
                    widths[3]
            );

            List<String> date = wrapText(
                    dateTime,
                    widths[4]
            );

            List<String> reason = wrapText(
                    appointment.reason(),
                    widths[5]
            );

            List<String> status = wrapText(
                    appointment.status(),
                    widths[6]
            );

            int lines = Math.max(
                    Math.max(
                            Math.max(id.size(), petId.size()),
                            Math.max(pet.size(), veterinarian.size())
                    ),
                    Math.max(
                            Math.max(date.size(), reason.size()),
                            status.size()
                    )
            );

            for (int i = 0; i < lines; i++) {
                System.out.println(
                        "| " + getWrappedLine(id, i, widths[0])
                                + " | " + getWrappedLine(petId, i, widths[1])
                                + " | " + getWrappedLine(pet, i, widths[2])
                                + " | " + getWrappedLine(veterinarian, i, widths[3])
                                + " | " + getWrappedLine(date, i, widths[4])
                                + " | " + getWrappedLine(reason, i, widths[5])
                                + " | " + getWrappedLine(status, i, widths[6])
                                + " |"
                );
            }

            // Display appointment time separately
            System.out.println(
                    "| " + padRight("", widths[0])
                            + " | " + padRight("", widths[1])
                            + " | " + padRight("", widths[2])
                            + " | " + padRight("Time: " + appointment.appointmentTime(),
                            widths[3])
                            + " | " + padRight("", widths[4])
                            + " | " + padRight("", widths[5])
                            + " | " + padRight("", widths[6])
                            + " |"
            );

            printTableBorder(widths);
        }
    }

    // Print table borders according to column widths
    private void printTableBorder(int[] widths) {
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
    private String getWrappedLine(
            List<String> lines,
            int index,
            int width
    ) {
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
}
